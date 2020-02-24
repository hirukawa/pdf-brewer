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
		\text ${見積書日付}\n
		\font sans-serif 10
		\text 見積書番号　
		\font serif 12
		\text ${見積書番号}

	\box 8 25 0 12
		\align left bottom
		\font serif bold 15
		\text ${見積先.名前}\n
	\box 8 42 0 0
		\font serif 12.5
		\line-height 2.5
		\text ${見積先.郵便番号}\n
		\line-height 2.0
		\text ${見積先.住所?replace("\n", "\\n")}\n \n
		\font sans-serif 11
		\text 　担当　
		\font serif 13
		\text ${見積先.担当者}

	\box -5 38 -100 0
		\align right top
		\font serif bold 15
		\text ${見積元.名前}\n
		\font serif 11
		\line-height 3.2
		\text ${見積元.郵便番号}\n
		\line-height 1.8
		\text ${見積元.住所?replace("\n", "\\n")}\n
		\font sans-serif 9
		\text 電話　 
		\font serif 11
		\text ${見積元.電話}\n
		\font sans-serif 9
		\text メール 
		\font serif 11
		\text ${見積元.メール}
	
	\box 18 85 -18 30
		\font serif 10.5
		\text 貴社益々ご隆盛のこととお慶び申し上げます。この度、貴社よりご依頼を頂きました\n \n \nの件につきまして、下記の通り見積申し上げます。
		\box 3 4 -3 10
			\align left center
			\font serif bold 12
			\text ${件名}

	\box 18 115 -18 30
		\box 0 0 60 10
			\align center right
			\font sans-serif 12
			\text 見　積　金　額
		\box 70 0 -0 10
			\align center left
			\font serif 12
			\text ${見積金額}
			\text  円 (消費税は別途)
		\box 0 10 60 10
			\align center right
			\font sans-serif 12
			\text 納　入　場　所
		\box 70 10 -0 10
			\align center left
			\font serif 12
			\text ${納入場所}
		\box 0 20 60 10
			\align center right
			\font sans-serif 12
			\text 納　入　期　日
		\box 70 20 -0 10
			\align center left
			\font serif 12
			\text ${納入期日}
		\box 0 30 60 10
			\align center right
			\font sans-serif 12
			\text 支　払　条　件
		\box 70 30 -0 10
			\align center left
			\font serif 12
			\text ${支払条件}
		\box 0 40 60 10
			\align center right
			\font sans-serif 12
			\text 見 積 有 効 期 間
		\box 70 40 -0 10
			\align center left
			\font serif 12
			\text ${見積有効期間}

	\box 18 180 -18 30
		\font serif 10.5
		\text 見積書金額には消費税は含まれておりません。\nお支払いの際は8％の消費税を付加してお支払いください。

\new-page

\box 12 12 -12 -12

	\box -0 0 -100 0
		\align right top
		\font sans-serif 10
		\text 見積書日付　
		\font serif 12
		\text ${見積書日付}\n
		\font sans-serif 10
		\text 見積書番号　
		\font serif 12
		\text ${見積書番号}

	\box 8 10 0 0
		\align left top
		\font serif bold 15
		\text 見積内訳

	\box 10 25 0 0
		\align left top
		\font sans-serif 12
		\text 1. 仕様条件
		\box 5 8 0 0
			\align left top
			\font serif 10.5
			\text ${仕様条件}

	\box 10 50 -10 0
		\align left top
		\font sans-serif 12
		\text 2. 作業内容
		
		\box 5 10 0 0
			\font sans-serif 10.5
			\text 期間　
			\font serif 10.5
			\text ${作業内容.期間}

		\box 4 20 -4 56
			\line-style thin solid
			\rect 0 0 0 0
			\line 0 6 -0 6
			\line -25 0 -25 56
			\box 1 0 -0 6
				\align center left
				\font sans-serif 10
				\text 作業内容/前提条件
			\box -0 0 -25 6
				\align center
				\font sans-serif 10
				\text 単位
			<#if 作業内容.明細?size gte 1 && 作業内容.明細[0].内容?has_content>
			\box 1 6 -26 10
				\align center left
				\font serif 10.5
				\text ${作業内容.明細[0].内容}
			\box 133 6 25 10
				\align center
				\font serif 10.5
				\text ${作業内容.明細[0].単位}
			</#if>
			<#if 作業内容.明細?size gte 2 && 作業内容.明細[1].内容?has_content>
			\box 1 10 -26 14
				\align center left
				\font serif 10.5
				\text ${作業内容.明細[1].内容}
			\box 133 10 25 14
				\align center
				\font serif 10.5
				\text ${作業内容.明細[1].単位}
			</#if>
			<#if 作業内容.明細?size gte 3 && 作業内容.明細[2].内容?has_content>
			\box 1 14 -26 18
				\align center left
				\font serif 10.5
				\text ${作業内容.明細[2].内容}
			\box 133 14 25 18
				\align center
				\font serif 10.5
				\text ${作業内容.明細[2].単位}
			</#if>
			
	\box 10 138 -10 0
		\align left top
		\font sans-serif 12
		\text 3. 納品物件
		\box 5 8 0 0
			\align left top
			\font serif 10.5
			\text ${納品物件}
		
	\box 10 163 -10 0
		\align left top
		\font sans-serif 12
		\text 4. 検収条件
		\box 5 8 0 0
			\align left top
			\font serif 10.5
			\text ${検収条件}
		
	\box 10 194 -0 5
		\align left bottom
		\font sans-serif 10
		\text 特記事項
	\box 10 200 -10 50
		\line-style thin solid
		\rect 0 0 0 0
		\box 3 5 -3 -5
		\align left top
		\font serif 10.5
		\line-height 2.3
		\text ${特記事項?replace("\n", "\\n")}




