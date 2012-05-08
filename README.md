gcouchdb
========

Wstęp:

Plik test.groovy to skrypt przerzucający dane między bazami danych Couchdb i SQLite. Został napisany w języku Groovy, który jest "nakładką" na język Java, ma przyjemniejszą składnie, działa na JVM i umożliwia - między innymi - korzystanie z wszystkich javowych bibliotek. W skrypcie dostęp do CouchDB zapewnia interfejs jcouchdb a relacyjną bazą danych jest SQLite.

Instalacja:

Wystarczy zainstalować interpreter języka Groovy, na Ubuntu: sudo apt-get install groovy. Resztę potrzebnych bibliotek zapewnia nam mechanizm Grape wbudowany w Groovy'ego, który śćiąga i cache'uje potrzebne pliku w folderze .groovy w katalogu domowym użytkownika. Ostatecznie znajdować się w nim będą pliki .jar dla bazy SQLite, interfejsu jcouchdb, parsowania plików XML i JSON, z których korzysta skrypt.

Użytkowanie: 

Klonujemy repozytorium do katalogu domowego: git clone git@github.com:ojuhas/gcouchdb.git. Powinny znajdować się w nim następujące pliki/foldery:

- database.xml - plik xml z bazą cytatów, które sparsujemy i wrzucimy do bazy CouchDB,
- test.groovy - plik z programem, jest to okienkowy interfejs do przerzucania "rekordów" między bazami SQLite i CouchDB,
- .groovy - folder z plikiem konfiguracyjnym grapeConfig.xml dla mechanizmu Grape znacznie przyspieszający jego pracę.

Następnie uruchamiamy nasz skrypt poleceniem: groovy test.groovy. Powinno pojawić się okno programu, gdzie najpierw wybieramy opcję "XML -> CouchDB", która wrzuci dane z pliku XML do bazy (domyślnie) "quotes" utworzonej na CouchDB. Później możemy przerzucić te dane do bazy SQLite (zostanie utworzony plik test.db w repozytorium), podejrzeć wynik w nowym oknie po kliknięciu przycisku "Tabela SQL" i ponownie załadować rekordy z SQLite'a do CouchDB.

Linki:

groovy - http://groovy.codehaus.org/
jcouchdb - http://code.google.com/p/jcouchdb/
sqlite - http://www.sqlite.org/
