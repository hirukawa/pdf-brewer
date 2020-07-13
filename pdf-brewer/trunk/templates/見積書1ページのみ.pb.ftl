\media A4
\box 12 12 -12 -12

	\box 0 2 0 0
		\align center top
		\font serif bold 21.5
		\text 見積書

	\box -0 12 -100 0
		\align right top
		\font sans-serif 10
		\text 見積書日付　
		\font serif 12
		\text ${見積書日付!}\n
		\font sans-serif 10
		\text 見積書番号　
		\font serif 12
		\text ${見積書番号!}

	\box 8 25 0 12
		\align left bottom
		\font serif bold 15
		\text ${見積先.名前!}\n
	\box 8 42 0 0
		\font serif 12.5
		\line-height 2.5
		\text ${見積先.郵便番号!}\n
		\line-height 2.0
		\text ${(見積先.住所!)?replace("\n", "\\n")}\n \n
		\font sans-serif 11
		\text 　担当　
		\font serif 13
		\text ${見積先.担当者!}

	\box -5 38 -100 0
		\align right top
		\font serif bold 15
		\text ${見積元.名前!}\n
		\font serif 11
		\line-height 3.2
		\text ${見積元.郵便番号!}\n
		\line-height 1.8
		\text ${(見積元.住所!)?replace("\n", "\\n")}\n
		\font sans-serif 9
		\text 電話　 
		\font serif 11
		\text ${見積元.電話!}\n
		\font sans-serif 9
		\text メール 
		\font serif 11
		\text ${見積元.メール!}
	
	\box 18 85 -18 30
		\font serif 10.5
		\text 貴社益々ご隆盛のこととお慶び申し上げます。この度、貴社よりご依頼を頂きました\n \n \nの件につきまして、下記の通り見積申し上げます。
		\box 3 4 -3 10
			\align left center
			\font serif bold 12
			\text ${件名!}

	\box 18 115 -18 30
		\box 0 0 60 10
			\align center right
			\font sans-serif 12
			\text 見　積　金　額
		\box 70 0 -0 10
			\align center left
			\font serif 12
			\text ${見積金額!}
			\text  円 (消費税は別途)
		\box 0 10 60 10
			\align center right
			\font sans-serif 12
			\text 納　入　場　所
		\box 70 10 -0 10
			\align center left
			\font serif 12
			\text ${納入場所!}
		\box 0 20 60 10
			\align center right
			\font sans-serif 12
			\text 納　入　期　日
		\box 70 20 -0 10
			\align center left
			\font serif 12
			\text ${納入期日!}
		\box 0 30 60 10
			\align center right
			\font sans-serif 12
			\text 支　払　条　件
		\box 70 30 -0 10
			\align center left
			\font serif 12
			\text ${支払条件!}
		\box 0 40 60 10
			\align center right
			\font sans-serif 12
			\text 見 積 有 効 期 間
		\box 70 40 -0 10
			\align center left
			\font serif 12
			\text ${見積有効期間!}

	\box 18 180 -18 30
		\font serif 10.5
		\text 見積書金額には消費税は含まれておりません。\nお支払いの際は8％の消費税を付加してお支払いください。
