# Kids_domaci_2


CAUSALNO SLANJE
	posalje se poruka (transaction npr) -> uhvati je SimpleServetnListener i posalje CausalBrodcastHandler-u
	CausalBrodcastHandler stavi poruku na u CausalBrodcastShared pending i onda ih checkuje
	Checkovanje pending poruka je da se prodju kroz poruke i u odnosu na VectorClock i tip poruke se posalje potrebnom Handleru

CLI-PARSER ucita comandu sa inputa
Aktivira komandu -> ona se da DELAYED-MESSAGE-SENDERU da posalje poruku
	usput on aktivira messageSentEffect (npr transakcija uradi add/take cakebits)

COMMANDS:{
	TRANSACTION -> {
		TransactinBurstCommand:
			-zapise da je trans poslata u CausalBrodcastShared
			-izvrsi send effect (take bitcakes)
			-inceremntuje clock
			-posalje se susedima
	}
	INFO -> {
		ispise stanje bitcake-ova i stanje vecto clock-a
	}
	BITCAKE-INFO -> {
		-start collecting info od suseda
		-pokrene snapshot workera i u zavisnosti od AB ili AV pokrene razlicite algoritme
		
		:AB: (handleri objasnjeni u causalBrodcastShared)
		-posalje svim susedima ASK-MESSAGE 
		-napravi rezultat za sebe i sacuva ga (increment clock)
		-ceka da svi susedi odtovore sa TELL-MESSAGE i da se sacuvaju poruke u mapu
		-kada se sacuvaju sve isprintaj rez
		
		:AV:
	}
}

SERVENT-LISTENER uhvati poruku (od delayedMsgSendera) i posalje je CausalBroadcastHandler

CAUSAL-MSG-HANDLER (posto causal) stavi message na pending i pokrene CHECK-PENDING funkciju
	zatim rebrodcastuje serventovim (koji je upravo primio msg) susedima message
	
CAUSAL-BRODCAST-SHARED / CHECK-PENDING:{
	u zavisnosti koja je posruka radi razlicite stvari
	-incrementuje vector clock svaki put

	TRANSACTION -> {
		-poziva TRANSACTION-HANDLER koji parsira kolicinu i i doda bitcakes na svoj racun
	}
	AB-ASK-MSG -> {
		-posalje se poruka AbAshHandleru
		-Handler napravi TELL-MESSAGE
		-incrementuje vector clock
		-posalje susedima tellMessage
	}
	AB-TELL-MSG -> {
		-prosledi se AbTellHandleru
		-on napravi SnapshotResult i sacuva ga u mapu trenutnih snapshot-ova
	}

}

AV tok programa

0.	koristi se drugacije znimanje transakcija za give i get

1.	u collector workeru
	podesi kanale za get i give na 0
	svima posalji ask message (token)
	increment clock
	
2.	kada svi prime ask msg (token)
	svim svojim kanalima za get i ask stavi 0
	setuj svoj trenutnu bitcake kolicinu
	svim susedima poslao DONE message
	increment clock
	
3.	primi se DONE message
	prosledi se done handleru, on sacuva poruku u DONE LISTU/MAPU u shared, (clear-ujemo je na kraju printa)
	
4.	collector worker
	ceka da se doneList.size bude isti kao cervent count
	kada jeste salje svima TERMINATE message
	i sebi stavi na pending msgs i check pending msgs
