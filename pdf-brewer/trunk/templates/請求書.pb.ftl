<#assign 消費税率 = 0.10>
<#assign 小計 = 0>
<#list 明細 as 行>
	<#if (行.単価!)?replace(",", "")?length gt 0 && (行.数量!)?replace(",", "")?length gt 0>
		<#assign 小計 += 行.単価?replace(",", "")?number * 行.数量?replace(",", "")?number>
	</#if>
</#list>
<#assign 消費税 = (小計 * 消費税率)?floor>

\media A4
\box 12 12 -12 -12

	\box 0 2 0 0
		\align center top
		\font serif bold 21.5
		\text 請求書
	
	\box -0 12 -100 0
		\align right top
		\font sans-serif 10
		\text 請求書日付　
		\font serif 12
		\text ${請求書日付!}\n
		\font sans-serif 10
		\text 請求書番号　
		\font serif 12
		\text ${請求書番号!}
	
	\box 8 28 0 0
		\align left top
		\font serif bold 15
		\text ${請求先.名前!}\n
		\font serif 12.5
		\line-height 2.5
		\text ${請求先.郵便番号!}\n
		\line-height 2.0
		\text ${(請求先.住所!)?replace("\n", "\\n")}
		
	<#if 請求先.担当者?has_content>
	\box 11 55 0 0
		\align left top
		\font sans-serif 11
		\text 担当　
		\font serif 13
		\text ${請求先.担当者!}
	</#if>
		
	\box 0 70 0 0
		\align left top
		\line-height 1.7
		\font serif 10.5
		\text 平素は格別のご高配を賜り、厚く御礼申し上げます。\n下記の通り、請求申し上げます。
	
	\box -5 38 -100 0
		\align right top
		\font serif bold 15
		\text ${請求元.名前!}\n
		\font serif 11
		\line-height 3.2
		\text ${請求元.郵便番号!}\n
		\line-height 1.8
		\text ${(請求元.住所!)?replace("\n", "\\n")}\n
		\font sans-serif 9
		\text 電話　 
		\font serif 11
		\text ${請求元.電話!}\n
		\font sans-serif 9
		\text メール 
		\font serif 11
		\text ${請求元.メール!}
	
	\box 0 88 -0 6.5
		\align left top
		\font sans-serif 12.5
		\text 請求金額　
		\font serif bold 15
		\text ${小計 + 消費税}
		\font sans-serif 12.5
		\text  円
		\box 65 0 0 0
			\text お支払い期限　
			\font serif 12.5
			\text ${支払期限!}
	\line-style thick
	\line 0 -0 -0 -0

	\box 0 104 -0 4
		\align left top
		\font sans-serif 9
		\box 1 0 0 0
			\text 品名/件名
		\align right top
		\box 127 0 21 0
			\text 単価
		\box 148 0 16 0
			\text 数量
		\box 164 0 21 0
			\text 金額
	\line-style medium
	\line 0 -0 -0 -0
	
	\line-style thin dotted
	\box 0 108 -0 11
		<#if 明細?size gte 1 && 明細[0].単価?has_content>
		\box 1 1.5 126 -1.5
			\align left top
			\font serif 10
			\text ${明細[0].上段!}
			\align left bottom
			\font serif 13
			\text ${明細[0].下段!}
		\box 127 0 21 -1.5
			\align right bottom
			\font serif 12.5
			\text ${明細[0].単価?replace(",", "")?number}
		\box 148 0 16 -1.5
			\align right bottom
			\font serif 12.5
			\text ${明細[0].数量?replace(",", "")?number}
		\box 164 0 21 -1.5
			\align right bottom
			\font serif 12.5
			\text ${明細[0].単価?replace(",", "")?number * 明細[0].数量?replace(",", "")?number}
		</#if>
	\line 0 -0 -0 -0
	
	\box 0 119 -0 11
		<#if 明細?size gte 2 && 明細[1].単価?has_content>
		\box 1 1.5 126 -1.5
			\align left top
			\font serif 10
			\text ${明細[1].上段!}
			\align left bottom
			\font serif 13
			\text ${明細[1].下段!}
		\box 127 0 21 -1.5
			\align right bottom
			\font serif 12.5
			\text ${明細[1].単価?replace(",", "")?number}
		\box 148 0 16 -1.5
			\align right bottom
			\font serif 12.5
			\text ${明細[1].数量?replace(",", "")?number}
		\box 164 0 21 -1.5
			\align right bottom
			\font serif 12.5
			\text ${明細[1].単価?replace(",", "")?number * 明細[1].数量?replace(",", "")?number}
		</#if>
	\line 0 -0 -0 -0
	
	\box 0 130 -0 11
		<#if 明細?size gte 3 && 明細[2].単価?has_content>
		\box 1 1.5 126 -1.5
			\align left top
			\font serif 10
			\text ${明細[2].上段!}
			\align left bottom
			\font serif 13
			\text ${明細[2].下段!}
		\box 127 0 21 -1.5
			\align right bottom
			\font serif 12.5
			\text ${明細[2].単価?replace(",", "")?number}
		\box 148 0 16 -1.5
			\align right bottom
			\font serif 12.5
			\text ${明細[2].数量?replace(",", "")?number}
		\box 164 0 21 -1.5
			\align right bottom
			\font serif 12.5
			\text ${明細[2].単価?replace(",", "")?number * 明細[2].数量?replace(",", "")?number}
		</#if>
	\line 0 -0 -0 -0
	
	\box 0 141 -0 11
		<#if 明細?size gte 4 && 明細[3].単価?has_content>
		\box 1 1.5 126 -1.5
			\align left top
			\font serif 10
			\text ${明細[3].上段!}
			\align left bottom
			\font serif 13
			\text ${明細[3].下段!}
		\box 127 0 21 -1.5
			\align right bottom
			\font serif 12.5
			\text ${明細[3].単価?replace(",", "")?number}
		\box 148 0 16 -1.5
			\align right bottom
			\font serif 12.5
			\text ${明細[3].数量?replace(",", "")?number}
		\box 164 0 21 -1.5
			\align right bottom
			\font serif 12.5
			\text ${明細[3].単価?replace(",", "")?number * 明細[3].数量?replace(",", "")?number}
		</#if>
	\line 0 -0 -0 -0
	
	\box -0 155 -85 10
		\box 2 0 -0 -2
		\align left bottom
		\font sans-serif 12.5
		\text 小計
		\box 0 0 -2 -2
		\align right bottom
		\font serif 12.5
		\text ${小計}
		\font sans-serif 12.5
		\text  円
	\line-style medium solid
	\line 0 -0 -0 -0
	
	\box -0 165 -85 10
		\box 2 0 -0 -2
		\align left bottom
		\font sans-serif 12.5
		\text 消費税
		\box 0 0 -2 -2
		\align right bottom
		\font serif 12.5
		\text ${消費税}
		\font sans-serif 12.5
		\text  円
	\line-style medium solid
	\line 0 -0 -0 -0

	\box -0 175 -85 10
		\box 2 0 -0 -2
		\align left bottom
		\font sans-serif 14
		\text 合計
		\box 0 0 -2 -2
		\align right bottom
		\font serif bold 15
		\text ${小計 + 消費税}
		\font sans-serif 12.5
		\text  円
	\line-style thick solid
	\line 0 -0 -0 -0
	
	
	\box 0 190 -0 5
		\align left bottom
		\font sans-serif 10
		\text 振込先
	\box 0 196 -0 27
		\box 3 5 -0 -0
		\align left top
		\font serif 12.5
		\line-height 2.1
		\text ${振込先?replace('\n', '\\n')}
	\line-style thin solid
	\rect 0 0 0 0
	
	\box 0 230 -0 5
		\align left bottom
		\font sans-serif 10
		\text 備考
	\box 0 236 -0 27
		\box 3 5 -0 -0
		\align left top
		\font serif 12.5
		\line-height 2.1
		\text ${備考?replace('\n', '\\n')}
	\line-style thin solid
	\rect 0 0 0 0
	
