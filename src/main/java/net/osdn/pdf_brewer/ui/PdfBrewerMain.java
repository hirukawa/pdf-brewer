package net.osdn.pdf_brewer.ui;

import java.awt.SplashScreen;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.prefs.Preferences;

import org.apache.pdfbox.pdmodel.PDDocument;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.stage.FileChooser.ExtensionFilter;
import net.osdn.pdf_brewer.BrewerData;
import net.osdn.pdf_brewer.PdfBrewer;
import net.osdn.util.fx.Dialogs;
import net.osdn.util.fx.input.KeyCombinations;

public class PdfBrewerMain extends Application {
	public static String APP_TITLE = "PDF BREWER";
	public static String APP_VERSION;
	public static String APP_WINDOW_TITLE;
	
	public static final Image IMAGE_APPLICATION_16PX = new Image(getResourceAsStream("/img/pdf-brewer-icon-16px.png"));

	public static final Image IMAGE_FILE_16PX = new Image(getResourceAsStream("/img/file-16px.png"));
	public static final Image IMAGE_SAVE_16PX = new Image(getResourceAsStream("/img/save-16px.png"));
	
	public static final Image IMAGE_PAGE_FIRST_BLACK_16PX = new Image(getResourceAsStream("/img/page-first-black-16px.png"));
	public static final Image IMAGE_PAGE_FIRST_WHITE_16PX = new Image(getResourceAsStream("/img/page-first-white-16px.png"));
	public static final Image IMAGE_PAGE_PREVIOUS_BLACK_16PX = new Image(getResourceAsStream("/img/page-previous-black-16px.png"));
	public static final Image IMAGE_PAGE_PREVIOUS_WHITE_16PX = new Image(getResourceAsStream("/img/page-previous-white-16px.png"));
	public static final Image IMAGE_PAGE_NEXT_BLACK_16PX = new Image(getResourceAsStream("/img/page-next-black-16px.png"));
	public static final Image IMAGE_PAGE_NEXT_WHITE_16PX = new Image(getResourceAsStream("/img/page-next-white-16px.png"));
	public static final Image IMAGE_PAGE_LAST_BLACK_16PX = new Image(getResourceAsStream("/img/page-last-black-16px.png"));
	public static final Image IMAGE_PAGE_LAST_WHITE_16PX = new Image(getResourceAsStream("/img/page-last-white-16px.png"));

	
	private static final int PREF_PDF_PANE_WIDTH = 612;
	private static final int PREF_PDF_PANE_HEIGHT = 841;
	
	private static volatile int count = 0;
	private static Stage stage;
	private static Preferences preferences = Preferences.userNodeForPackage(PdfBrewerMain.class);
	private static File lastSaveDir;
	
	public static void main(String[] args) {
		if(count++ == 0) {
			int[] version = getApplicationVersion();
			if(version != null) {
				if(version[2] == 0) {
					APP_VERSION = String.format("%d.%d", version[0], version[1]);
				} else {
					APP_VERSION = String.format("%d.%d.%d", version[0], version[1], version[2]);
				}
				APP_WINDOW_TITLE = APP_TITLE + " " + APP_VERSION;
			} else {
				APP_WINDOW_TITLE = APP_TITLE;
			}
			launch(args);
		} else {
			Platform.runLater(() -> {
				if(stage != null) {
					stage.setIconified(false);
					stage.toFront();
				}
			});
		}
	}
	
	public static InputStream getResourceAsStream(String name) {
		return PdfBrewerMain.class.getResourceAsStream(name);
	}
	
	public static int[] getApplicationVersion() {
		String s = System.getProperty("java.application.version");
		if(s == null || s.trim().length() == 0) {
			return null;
		}
		
		s = s.trim() + ".0.0.0.0";
		String[] array = s.split("\\.", 5);
		int[] version = new int[4];
		for(int i = 0; i < 4; i++) {
			try {
				version[i] = Integer.parseInt(array[i]);
			} catch(NumberFormatException e) {
				e.printStackTrace();
			}
		}
		if(version[0] == 0 && version[1] == 0 && version[2] == 0 && version[3] == 0) {
			return null;
		}
		return version;
	}
	
	
	protected MenuBar menuBar;
	protected MenuItem menuFileOpen;
	protected MenuItem menuFileSave;
	protected MenuItem menuFileExit;
	protected Menu menuPdfFirst;
	protected Menu menuPdfPrevious;
	protected Menu menuPdfNext;
	protected Menu menuPdfLast;
	protected Menu menuPdfPageNumber;
	protected Label pdfPageNumberLabel;
	protected PdfPane pdfPane;
	
	private File input;
	private PDDocument document;
	
	private ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			return t;
		}
	});
	private BrewerTask task;
	
	@Override
	public void start(Stage stage) throws Exception {
		PdfBrewerMain.stage = stage;
		stage.getIcons().add(IMAGE_APPLICATION_16PX);
		stage.setTitle(PdfBrewerMain.APP_TITLE + ((PdfBrewerMain.APP_VERSION != null) ? (" " + PdfBrewerMain.APP_VERSION) : ""));
		stage.setMinWidth(256);
		stage.setMinHeight(380);
		stage.setScene(createScene());
		
		Rectangle2D screen = Screen.getPrimary().getVisualBounds();
		if(screen.getWidth() >= PREF_PDF_PANE_WIDTH && screen.getHeight() >= PREF_PDF_PANE_HEIGHT) {
			pdfPane.setPrefSize(PREF_PDF_PANE_WIDTH, PREF_PDF_PANE_HEIGHT);
		} else {
			pdfPane.setPrefSize(PREF_PDF_PANE_WIDTH / 2, PREF_PDF_PANE_HEIGHT / 2);
		}
		
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent event) {
				stage_onCloseRequest(event);
			}
		});

		stage.show();
		
		SplashScreen splash = SplashScreen.getSplashScreen();
		if(splash != null) {
			splash.close();
		}
	}
	
	protected Scene createScene() {
		
		menuBar = createMenuBar();

		pdfPane = new PdfPane();
		pdfPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				try {
					pdf_mouseEntered(event);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		pdfPane.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				try {
					pdf_mouseExited(event);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		pdfPane.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				try {
					pdf_mouseMoved(event);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		pdfPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				try {
					pdf_mouseClicked(event);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		pdfPane.scaleProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				try {
					pdf_scaleChanged(newValue.floatValue());
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
        
		BorderPane bp1 = new BorderPane();
		bp1.setCenter(pdfPane);
		bp1.setTop(menuBar);
		
		Scene scene = new Scene(bp1);
		scene.setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				try {
					onDragOver(event);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		scene.setOnDragDropped(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				try {
					onDragDropped(event);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.HOME), new Runnable() {
			@Override
			public void run() {
				if(!menuPdfFirst.isDisable()) {
					try {
						pdf_moveFirstPage();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		scene.getAccelerators().put(
			new KeyCombinations(
				new KeyCodeCombination(KeyCode.PAGE_UP),
				new KeyCodeCombination(KeyCode.UP),
				new KeyCodeCombination(KeyCode.KP_UP),
				new KeyCodeCombination(KeyCode.LEFT),
				new KeyCodeCombination(KeyCode.KP_LEFT)
			),
			new Runnable() {
				@Override
				public void run() {
					if(!menuPdfPrevious.isDisable()) {
						try {
							pdf_movePreviousPage();
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		);
		scene.getAccelerators().put(
			new KeyCombinations(
				new KeyCodeCombination(KeyCode.PAGE_DOWN),
				new KeyCodeCombination(KeyCode.DOWN),
				new KeyCodeCombination(KeyCode.KP_DOWN),
				new KeyCodeCombination(KeyCode.RIGHT),
				new KeyCodeCombination(KeyCode.KP_RIGHT)
			),
			new Runnable() {
				@Override
				public void run() {
					if(!menuPdfNext.isDisable()) {
						try {
							pdf_moveNextPage();
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.END), new Runnable() {
			@Override
			public void run() {
				if(!menuPdfLast.isDisable()) {
					try {
						pdf_moveLastPage();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		String pkg = PdfBrewerMain.class.getPackage().getName();
		URL url = PdfBrewerMain.class.getResource("/" + pkg.replace('.', '/') + "/stylesheet.css");
		if(url != null) {
			scene.getStylesheets().add(url.toExternalForm());
		}
		return scene;
	}
	
	protected MenuBar createMenuBar() {
		MenuBar menuBar = new MenuBar();

		Menu menuFile = new Menu("ファイル");
		menuFile.setId("file");
		menuFileOpen = new MenuItem("開く...", new ImageView(IMAGE_FILE_16PX));
		menuFileOpen.setId("open");
		menuFileOpen.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
		menuFileOpen.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					menu_open();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		menuFileSave = new MenuItem("名前を付けて保存...", new ImageView(IMAGE_SAVE_16PX));
		menuFileSave.setId("save");
		menuFileSave.setDisable(true);
		menuFileSave.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
		menuFileSave.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					menu_save();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		menuFileExit = new MenuItem("終了");
		menuFileExit.setId("exit");
		menuFileExit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					menu_exit();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		menuFile.getItems().addAll(
			menuFileOpen,
			menuFileSave,
			new SeparatorMenuItem(),
			menuFileExit
		);
		
		pdfPageNumberLabel = new Label("");
		StackPane sp = new StackPane(pdfPageNumberLabel);
		sp.setMinWidth(40);
		StackPane.setAlignment(pdfPageNumberLabel, Pos.CENTER);
		menuPdfPageNumber = new Menu("", sp);
		menuPdfPageNumber.setId("pdf-page-number");
		
		PagerButton btnFirst = new PagerButton(IMAGE_PAGE_FIRST_BLACK_16PX, IMAGE_PAGE_FIRST_WHITE_16PX);
		btnFirst.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					pdf_moveFirstPage();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		menuPdfFirst = new PagerMenu(btnFirst);
		
		PagerButton btnPrevious = new PagerButton(IMAGE_PAGE_PREVIOUS_BLACK_16PX, IMAGE_PAGE_PREVIOUS_WHITE_16PX);
		btnPrevious.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					pdf_movePreviousPage();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		menuPdfPrevious = new PagerMenu(btnPrevious);
		
		PagerButton btnNext = new PagerButton(IMAGE_PAGE_NEXT_BLACK_16PX, IMAGE_PAGE_NEXT_WHITE_16PX);
		btnNext.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					pdf_moveNextPage();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		menuPdfNext = new PagerMenu(btnNext);

		PagerButton btnLast = new PagerButton(IMAGE_PAGE_LAST_BLACK_16PX, IMAGE_PAGE_LAST_WHITE_16PX);
		btnLast.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					pdf_moveLastPage();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		menuPdfLast = new PagerMenu(btnLast);
		
		menuPdfFirst.setDisable(true);
		menuPdfPrevious.setDisable(true);
		menuPdfNext.setDisable(true);
		menuPdfLast.setDisable(true);
		menuPdfPageNumber.setDisable(true);
		menuPdfFirst.setVisible(false);
		menuPdfPrevious.setVisible(false);
		menuPdfNext.setVisible(false);
		menuPdfLast.setVisible(false);
		menuPdfPageNumber.setVisible(false);

		menuBar.getMenus().addAll(
			menuFile,
			menuPdfFirst,
			menuPdfPrevious,
			menuPdfPageNumber,
			menuPdfNext,
			menuPdfLast
		);
		return menuBar;
	}
	
	protected void stage_onCloseRequest(WindowEvent event) {
		if(document != null) {
			try { document.close(); } catch(Exception e) {}
			document = null;
		}
	}
	
	protected void onDragOver(DragEvent event) {
		if(event.getDragboard().hasFiles()) {
			event.acceptTransferModes(TransferMode.COPY);
		} else {
			event.consume();
		}
	}
	
	protected void onDragDropped(DragEvent event) {
		Dragboard db = event.getDragboard();
		boolean success = false;
		if(db.hasFiles()) {
			List<File> files = db.getFiles();
			if(files.size() == 1) {
				File file = files.get(0);
				String ext = file.getName().toLowerCase();
				int i = ext.lastIndexOf('.');
				if(i >= 0) {
					ext = ext.substring(i);
				}
				if(ext.equals(".yml") || ext.equals(".yaml") || ext.equals(".pb")) {
					try {
						data_open(file);
						stage.toFront();
					} catch(Exception e) {
						e.printStackTrace();
					}
					success = true;
				}
			}
		}
		event.setDropCompleted(success);
		event.consume();
	}
	
	protected void menu_open() throws Exception {
		FileChooser fc = new FileChooser();
		fc.setTitle("開く");
		fc.getExtensionFilters().add(new ExtensionFilter("YAML", "*.yml", "*.yaml"));
		fc.getExtensionFilters().add(new ExtensionFilter("PDF Brewer", "*.pb"));
		String s = preferences.get("lastOpenDirectory", null);
		if(s != null) {
			File dir = new File(s);
			if(dir.exists() && dir.isDirectory()) {
				fc.setInitialDirectory(dir);
			}
		}
		File file = fc.showOpenDialog(stage);
		if(file != null) {
			String ext = file.getName().toLowerCase();
			int i = ext.lastIndexOf('.');
			if(i >= 0) {
				ext = ext.substring(i);
			}
			if(ext.equals(".yml") || ext.equals(".yaml") || ext.equals(".pb")) {
				data_open(file);
			}
			preferences.put("lastOpenDirectory", file.getParentFile().getAbsolutePath());
		}
	}
	
	protected void menu_save() throws Exception {
		if(document == null) {
			return;
		}
		
		FileChooser fc = new FileChooser();
		fc.setTitle("名前を付けて保存");
		if(lastSaveDir != null && lastSaveDir.isDirectory() && lastSaveDir.exists()) {
			fc.setInitialDirectory(lastSaveDir);
		} else {
			lastSaveDir = null;
			if(input != null) {
				fc.setInitialDirectory(input.getParentFile());
			}
		}
		String defaultName = "output.pdf";
		if(input != null) {
			defaultName = input.getName();
			int i = defaultName.lastIndexOf('.');
			if(i > 0) {
				defaultName = defaultName.substring(0, i);
			}
			defaultName += ".pdf";
		}
		fc.setInitialFileName(defaultName);
		File file = fc.showSaveDialog(stage);
		if(file != null) {
			lastSaveDir = file.getParentFile();
			if(document != null) {
				try {
					document.save(file);
				} catch (IOException e) {
					e.printStackTrace();
					Dialogs.showError(stage, e.getLocalizedMessage());
				}
			}
		}
	}
	
	protected void menu_exit() throws Exception {
		Platform.exit();
	}

	protected void data_open(File file) throws Exception {
		pdfPane.getSpinnerImageView().setVisible(true);
		stage.setTitle(APP_WINDOW_TITLE);
		menuFileSave.setDisable(true);
		if(task != null) {
			task.cancel(false);
		}
		input = file;
		task = new BrewerTask(new BrewerCallable(input));
		document = null;
		pdfPane.setDocument(document);
		executor.execute(task);
	}
	
	protected void pb_open(File file) throws Exception {
		pdfPane.getSpinnerImageView().setVisible(true);
		stage.setTitle(APP_WINDOW_TITLE);
		menuFileSave.setDisable(true);
		if(task != null) {
			task.cancel(false);
		}
		input = file;
		task = new BrewerTask(new BrewerCallable(input));
		document = null;
		pdfPane.setDocument(document);
		executor.execute(task);
	}
	
	protected void pdf_moveFirstPage() throws Exception {
		PDDocument document = pdfPane.getDocument();
		int pageIndex = 0;
		if(document != null) {
			pdfPane.setPage(pageIndex);
		}
		updatePagerButtons(document, pageIndex);
	}
	
	protected void pdf_movePreviousPage() throws Exception {
		PDDocument document = pdfPane.getDocument();
		int pageIndex = -1;
		if(document != null) {
			pageIndex = pdfPane.getPageIndex();
			if(pageIndex > 0) {
				pdfPane.setPage(--pageIndex);
			}
		}
		updatePagerButtons(document, pageIndex);
	}
	
	protected void pdf_moveNextPage() throws Exception {
		PDDocument document = pdfPane.getDocument();
		int pageIndex = -1;
		if(document != null) {
			pageIndex = pdfPane.getPageIndex();
			if(pageIndex + 1 < document.getNumberOfPages()) {
				pdfPane.setPage(++pageIndex);
			}
		}
		updatePagerButtons(document, pageIndex);
	}
	
	protected void pdf_moveLastPage() throws Exception {
		PDDocument document = pdfPane.getDocument();
		int pageIndex = -1;
		if(document != null) {
			pageIndex = document.getNumberOfPages() - 1;
			pdfPane.setPage(pageIndex);
		}
		updatePagerButtons(document, pageIndex);
	}
	
	protected void pdf_mouseEntered(MouseEvent event) throws Exception {
	}
	
	protected void pdf_mouseExited(MouseEvent event) throws Exception {
	}
	
	protected void pdf_mouseMoved(MouseEvent event) throws Exception {
	}
	
	protected void pdf_mouseClicked(MouseEvent event) throws Exception {
	}
	
	protected void pdf_scaleChanged(float scale) throws Exception {
	}
	
	protected void updatePagerButtons(PDDocument document, int pageIndex) {
		if(document == null || pageIndex < 0) {
			menuPdfFirst.setDisable(true);
			menuPdfPrevious.setDisable(true);
			menuPdfNext.setDisable(true);
			menuPdfLast.setDisable(true);
			menuPdfPageNumber.setText("");
			
			menuPdfFirst.setVisible(false);
			menuPdfPrevious.setVisible(false);
			menuPdfNext.setVisible(false);
			menuPdfLast.setVisible(false);
			menuPdfPageNumber.setVisible(false);
			return;
		} else {
			menuPdfFirst.setVisible(true);
			menuPdfPrevious.setVisible(true);
			menuPdfNext.setVisible(true);
			menuPdfLast.setVisible(true);
			menuPdfPageNumber.setVisible(true);
		}
		if(pageIndex > 0) {
			menuPdfFirst.setDisable(false);
			menuPdfPrevious.setDisable(false);
		} else {
			menuPdfFirst.setDisable(true);
			menuPdfPrevious.setDisable(true);
		}
		if(pageIndex + 1 < document.getNumberOfPages()) {
			menuPdfNext.setDisable(false);
			menuPdfLast.setDisable(false);
		} else {
			menuPdfNext.setDisable(true);
			menuPdfLast.setDisable(true);
		}
		pdfPageNumberLabel.setText((pageIndex + 1) + " / " + document.getNumberOfPages());
	}
	
	private class BrewerTask extends FutureTask<PDDocument> {
		private BrewerCallable callable;

		public BrewerTask(BrewerCallable callable) {
			super(callable);
			this.callable = callable;
		}

		@Override
		protected void done() {
			if(!isCancelled()) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						try {
							if(document != null) {
								try { document.close(); } catch(Exception e) {}
								document = null;
							}
							document = get();
							pdfPane.setDocument(document);
							pdfPane.setPage(0);
							if(document != null) {
								stage.setTitle(callable.input.getAbsolutePath() + " - " + APP_WINDOW_TITLE);
								menuFileSave.setDisable(false);
							}
							updatePagerButtons(document, 0);
							pdfPane.getSpinnerImageView().setVisible(false);
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				});
			} else {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						try {
							updatePagerButtons(document, 0);
							pdfPane.getSpinnerImageView().setVisible(false);
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			callable.isCanceled = true;
			return super.cancel(mayInterruptIfRunning);
		}
	}
	
	private class BrewerCallable implements Callable<PDDocument> {
		private File input;
		private boolean isCanceled;

		public BrewerCallable(File input) {
			this.input = input;
		}
		
		@Override
		public PDDocument call() throws Exception {
			try {
				BrewerData pb = new BrewerData(input);
				PdfBrewer brewer = new PdfBrewer();
				brewer.process(pb);
				if(isCanceled) {
					return null;
				}
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				brewer.save(output);
				if(isCanceled) {
					return null;
				}
				return PDDocument.load(output.toByteArray());
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}
