gcouchdb
========

Wstęp:

Plik test.groovy to skrypt przerzucający dane między bazami Couchdb i SQLite. Został napisany w języku Groovy, który jest "nakładką" na język Java, ma przyjemniejszą i prostsza składnię (ogólnie jest językiem dynamiczie typowanym z ciekawym  mechanizmem "domknięć"), działa na JVM i umożliwia - między innymi - korzystanie ze wszystkich bibliotek javowych. W skrypcie dostęp do baz CouchDB zapewnia interfejs jcouchdb, relacyjną bazą danych jest SQLite a prosta "okienkowość" oparta jest na Swingu.

Instalacja:

Żeby uruchomić skrypt wystarczy zainstalować interpreter języka Groovy. Na Ubuntu realizuje to polecenie: sudo apt-get install groovy. Resztę potrzebnych bibliotek zapewnia nam mechanizm Grape wbudowany w Groovy'ego, który przy pierwszym uruchomieniu ściąga a następnie cache'uje potrzebne pliki w folderze .groovy w katalogu domowym użytkownika. Ostatecznie znajdować się w nim będą pliki .jar dla bazy SQLite, interfejsu jcouchdb oraz parsowania plików XML i JSON, które są ładowane w programie zwykłymi javowymi "importami".

Użytkowanie: 

Klonujemy repozytorium do katalogu domowego poleceniem: git clone git@github.com:ojuhas/gcouchdb.git. W ściągniętym repozytorium powinny znajdować się następujące pliki/foldery:

- database.xml - plik xml z bazą cytatów, które sparsujemy i wrzucimy do bazy CouchDB,
- test.groovy - plik programu, jest to okienkowy interfejs do przerzucania "rekordów" między bazami SQLite i CouchDB,
- .groovy - folder z plikiem konfiguracyjnym grapeConfig.xml dla mechanizmu Grape znacznie przyspieszający jego pracę.

Po odpaleniu serwera CouchDB ruchamiamy skrypt poleceniem: groovy test.groovy. W oknie aplikacji najpierw klikamy na przycisk "XML -> CouchDB", co przerzuci dane z pliku XML do (domyślnie) bazy "quotes" stworzonej na CouchDB. Później kopiujemy dane do bazy SQLite przyciskiem "CouchDB -> SQLite" (zostanie utworzony plik test.db w naszym repozytorium) i podejrzeć wynik w nowym oknie po wybraniu opcji "Tabela SQL". Na końcu możemy ponowne załadować rekordy z SQLite'a do CouchDB poprzez wybór "SQLite -> CouchDB".

Linki:

- groovy - http://groovy.codehaus.org/, http://groovy.codehaus.org/Closures+-+Formal+Definition
- jcouchdb - http://code.google.com/p/jcouchdb/
- sqlite - http://www.sqlite.org/
