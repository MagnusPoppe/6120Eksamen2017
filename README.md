# 6120Eksamen2017
Eksamensoppgave 2017 For faget 6120 Applikasjonutvikling med Mobile Enheter v/ Høgskolen i Sørøst Norge

__NOTE! ALL CODE IN NORWEGIAN, AS THE EXAM REQUIRED.__


## OPPBYGGING
Hele applikasjonen er bygd opp etter prinsippet MVC (Model-View-Controller). Som modell er dette pakken “datamodell” 
som inneholder alle data applikasjonen bruker. Kontrolleren er “ActivityCtrl” som ligger i bakgrunn for alle 
fragmenter. Tilslutt er presentasjonen, med fragmenter som styres av Kontrolleren. UML diagrammet under viser dette 
visuelt.

![alt text](https://github.com/MagnusPoppe/6120Eksamen2017/blob/master/UML%20diagram%202.png?raw=true "UML DIAGRAM OF APP")

En en slik applikasjon der fragmenter styrer alt det visuelle er “back-stack” til fragmentene veldig viktig. Denne styrer 
hva som skjer når man trykker på tilbakeknappen. All navigasjon bakover i applikasjonen gjøres ved hjelp av androids 
innebygde tilbakeknapp. Backstack er også styrt smart. Det skal alltid være som forventet hva som skjer da man trykker på 
tilbakeknappen.

Med ArrayList av Destinasjonsobjekter har kontrolleren kontakt med modell laget. Når operasjoner mot modellen skal gjøres, 
skal dette alltid skje gjennom kontrolleren. Ingen “Views” skal direkte kunne endre modellen.
Hver av klassene i prosjektet har en javadoc kommentar over klassedeklarasjonen. Disse kommentarene forklarer hver av 
klassenes hovedfunksjon.

### DESIGNBIBLIOTEKET
Noen av komponentene inni appen, spesielt i “FragmentAddLocation” er fra androids design bibliotek 
(‘com.android.support:design:25.3.0’). Dette er et av de mest brukte support bibliotekene til Android. 
Her finner man massevis av nye og kule komponenter som ikke vanligvis er i bruk. Av disse er jeg veldig glad i SnackBar 
som er hyppig brukt i koden. Denne er til erstatning for “Toast” og utvider bruken med mulighet for bruker interaksjon. 
Man kan legge til f.eks. knapper osv. Jeg har også brukt andre komponenter som “TextInputEditText” for mer fancy inndata 
felter.

## EGET API
Jeg valgte å lage mitt eget PHP baserte rest API. Dette gjorde jeg fordi jeg allerede er vant med å bygge opp REST APIer 
fra jobb og andre skolefag. Dette var dermed en enklere løsning som tok kortere tid.

APIet er bygget opp ved at du har en ruter (Router funksjonen) som tolker adressen som har blitt kalt opp. Gjennom IF 
setninger finner den ut, skråstrek for skråstrek hvor brukeren vil med URLen. Ruteren gjør en splitt på adressen med “/“ 
i fokus. Dermed får man ut hvert enkelt katalog som URLen består av. Etter ønsket katalog er funnet vil forespørselen 
så redirigere til en funksjon som gjør ønsket operasjon utifra HTTP metoden som brukeren valgte (GET / POST er de eneste 
støttede for denne oppgaven). Hver fungerende URL har en funksjon som håndterer hva som skal skje ved forespørsel til den.

Et REST api skal alltid gi tilbake en god respons kode, og det gjør dette også. Om man skriver en dårlig adresse vil APIet
svare med “502 BAD GATEWAY”. Om man sender inn til en korrekt adresse, men ikke har riktig søkeord vil man få “404 NOT FOUND”
. Om det var en lovlig og fungerende forespørsel, får man “200 OK” tilbake.

![alt text](https://github.com/MagnusPoppe/6120Eksamen2017/blob/master/erdiagram.png?raw=true "Database model")

Databasen er ganske standard. Om brukeren blir kun e-post lagret. Den er modellert slik at en bruker kan inneholde mye 
er informasjon om ønsket. Dette er enkelt å utvide til. Se databasen over i E/R diagrammet. Det er også vedlagt dump av 
databasen.

#### API KAN TESTES GJENNOM DISSE URLENE:

__For uthenting av turmål (Bruker HTTP GET):__
```
http://itfag.usn.no/~210852/api.php/destination/                  // Gir alle
http://itfag.usn.no/~210852/api.php/destination/<heltall>         // Gir den med oppgitt ID
http://itfag.usn.no/~210852/api.php/destination/<brukernavn>      // Gir alle for en bruker
```
__For å sette inn i database (Bruke HTTP POST):__
```
http://itfag.usn.no/~210852/api.php/destination/ // Med jsonformatert objekt av data.
```

## DE FORSKJELLIGE FRAGMENTENE

### KARTET
Kartet er alltid synlig for applikasjonen og er felles faktoren for hele appen. For å vise dette har jeg brukt Google 
sitt Maps Android API. Dette gir innebygde kart til applikasjonen. Kartet vises i et eget fragment, som er konfigurert
for å være kompatibel med mitt GUI. Når man interagerer med kartet (f.eks. trykker på en markør) svarer applikasjonen 
med relevante data. Kartet er alltid øverst i “viewet” for portrett modus, eller til høyre for landskapsmodus. Dette 
er fordi det alltid er relevant til applikasjonen. Under hvert av de andre skjermbildene er bruken av kart forklart.

### TURMÅL LISTEN
Turmål listen inneholder en liste over registrerte turmål liggende i databasen. Denne listen er sortert etter nærmeste
destinasjon. Kartet og Turmål listen er to sider av samme skal, siden begge disse brukes for å se på oversikt over de 
forskjellige Turmålene. Per turmål vises navn, type og høyde (meter over havet). Listen består av en ListView med en 
egen adapter og egen Layout. Om man klikker på en av dem får man opp en detaljert visning av turmålet.

### DETALJERT VISNING
Den detaljerte visningen består av en øvre del, som tilsvarer informasjonen vist i listview elementene. Denne inneholder 
da det samme som listview elementene, altså navn, type og m.o.h. Under dette finner man et bilde av stedet, om bilde er 
tatt. Under der igjen finner man skaperens epost-adresse (epost blir brukt som brukernavn). Når man åpner detaljert
visning sender man med objektet som skal presenteres ekstra godt. Dette er lagret i kontrolleren, så det eneste man faktisk 
sender over er indeksen til objektet. Kontrolleren leverer så objektet som skal brukes.

Åpningen av visningen gjør også at panelet eller “vinduet” vokser med ca. 20%. Dette er gjort både på registreringen og 
på detalj panelet. Dette gjøres så fokuset til brukeren skal skifte fra kartet til panelet. Tanken er altså da at det som
er i fokus skal ha større areal av skjermen. Når listen vises er dette kartet, mens når de andre panelene vises (legg til 
og detaljert visning), skal de være i fokus. Endringen i størrelse skjer kun på “medium” og “små” skjermer (etter Android
APIets definisjon).

### REGISTRERING AV NYE TURMÅL
Denne siden er en ganske standard registreringsside. Her finner man tre felter for enkel oppføring av navn, type turmål 
og beskrivelse. Det er ingen inndata sanitering, og appen og databasen støtter nullpunkter. Det er dermed ikke behov for
dette. Feltene for posisjons informasjon er automatisk satt ved åpningen av fragmentet og endres ikke. Kartet brukes her 
til å presentere posisjonen som skal lagres.

Det er sikret i åpningsmetoden for fragmentet at det må være to ting på plass før man for lov til å legge inn nye turmål. 
Man MÅ ha registrert en epost i innstillinger. Om dette ikke er gjort, vil man bli bedt om å gjøre det. Man må også ha 
enhetens posisjon for å registrere.

Bilde lagres ved bruk av FileProvider klassen, og var noe av det vanskeligste med utviklingen av appen. Dette mønsteret 
ble brukt fordi mønsteret brukt i forelesning er utdatert. Bildet lagres først lokalt, så lastes det opp til tjeneren om 
mulig. Jeg var nødt til å bruke en annen tjener enn itfag.usn.no grunnet manglende rettigheter (ref epost). Se skriptet 
for mottagelse av bilder under “API/6120Images/uploadImage.php” i innleveringsmappen.

Når man lagrer et turmål (destinasjon) vil appen håndtere databaseopplastingen automatisk. Når appen har nett vil dette 
gjøres direkte til global database (betjent på itfag.usn.no), og uten nett vil appen automatisk lagre i lokal SQLite 
database. Oppføringene som lagres lokalt vil bli automatisk lastet opp til databasen ved neste app-oppstart eller ved 
andre anledninger.

## HVORDAN BRUKE
For å se detaljvisningen av appen kan man enten klikke på et av elementene i listen, eller på en av markørene på kartet.
Begge vil gi deg samme skjermbilde. For å komme tilbake til listen klikker man enkelt på tilbakeknappen.

![alt text](https://github.com/MagnusPoppe/6120Eksamen2017/blob/master/Skjermbilde%20legg%20til.png?raw=true "how to photo 01")

For å legge til et nytt element i listen klikker man på den store rosa “Floating action button” knappen med plusstegn 
inni. Dette vil åpne opp et skjema man kan fylle ut med informasjonen man ønsker. Det er 4 ting man kan fylle ut, navn, 
beskrivelse og type, i tillegg til et bilde man kan ta med kamera. Øvrig informasjon om lokasjonen blir satt automagisk. 
Disse er koordinater og høyde over havet.  

![alt text](https://github.com/MagnusPoppe/6120Eksamen2017/blob/master/Skjermbilde%20detaljer.png?raw=true "how to photo 02")

## REFERANSER
Jeg lister opp referanser her. De samme referansene er også lenket til i kommenarer i koden der de hører til, for 
enkelhetens skyld.

Google maps api: Jeg har brukt veldig mange av undersidene her. De fleste har blitt aksessert gjennom google.no.
https://developers.google.com/maps/documentation/android-api/

Metode for å sjekke om applikasjonen kjører på nettbrett:
https://stackoverflow.com/questions/5015094/how-to-determine-device-screen-size-category-small- normal-large-xlarge-usin/19256468#19256468

Mønster for lagring av bilder:
https://stackoverflow.com/questions/17674634/saving-and-reading-bitmaps-images-from-internal- memory-in-android

Metode for lagring av bilder over nett og PHP skript for mottak av bilder på tjener:
https://stackoverflow.com/questions/23921356/android-upload-image-to-php-server

Mønster brukt for bildetakning og hjelp til løsning:
https://developer.android.com/reference/android/support/v4/content/FileProvider.html https://stackoverflow.com/questions/38555301/android-taking-picture-with-fileprovider

Lenker for bruk læring av databaser (læreboken):
https://developer.android.com/training/basics/data-storage/databases.html
