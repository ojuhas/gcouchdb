import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.HttpRequest
import org.jcouchdb.document.*
import org.jcouchdb.exception.*
import org.jcouchdb.util.Assert
import org.jcouchdb.db.*
import org.svenson.*
import org.slf4j.*
import groovy.sql.Sql
import org.sqlite.SQLite
import groovy.swing.SwingBuilder
import javax.swing.*
import java.awt.*

import static javax.swing.JFrame.EXIT_ON_CLOSE 
import javax.swing.SwingConstants

// powyżej są importy bibliotek Javy i Groovy'ego do obsługi SQLite'a, Swinga czy JCouchDB

/* Grapes to mechanizm wbudowany w Groovy'ego, dzieki ktoremu rozwiazywane sa problemy z brakujacymi bibliotekami. Wszystkie pliki sciagane sa z repozytorium Mavena na serwerze mvnrepository.com poleceniem Grab, nas interesuja SQLite oraz JCouchDB.
*/

@Grapes([    
	@Grab(group='org.xerial', module='sqlite-jdbc', version='3.7.2'),
	@Grab(group='com.google.code.jcouchdb',module='jcouchdb',version='0.10.0-3'),
	@GrabConfig(systemClassLoader=true)
])

def count1=0
def count2=0
def count3=0

def swing = new SwingBuilder()

/* Metoda przetwarzajaca dany "rekord" na JSONA, obiekt doc jest typu HashMap, bo JSONy maja strukture klucz-wartosc. Z kazdego podanego do funkcji "rekordu" wyluskujemy tytul, cyctat itp. i wrzucamy je pod odpowiedni klucz w obiekcie doc. Na koncu calego nowoutworzonego JSONa doklejamy do reszty JSONow. Ogolnie tworzymy jednego wielkiego JSONa, ktorego pozniej za jednym razem wrzucimy do bazy, bo jest to szybsze niz dodawanie kazdego osobno. */

def Put_To_CouchDB(a,b)
{

	Map<String,String> doc = new HashMap<String, String>(); //tu jest tworzony JSON jako mapa
	doc.put("title", a.title as String);
	doc.put("quote", a.quote as String);
	doc.put("author", a.author as String);
	doc.put("subject", a.subject as String);
	b.add(doc) //tu jest dodawanie nowego JSONA do tego "globalnego"
}

/* Metoda parsujaca plik database.xml bedacego nasza pierwotna baza danych. Tworzenie "rekordow", ktore pozniej wrzucimy do CouchDB, jest bardzo proste, co pokazuje przewage Groovy'ego nad Java, gdzie trzeba pisac multum linii kodu do obslugi XMLi. Pozniej laczymy sie z serwerem CouchDB i tworzymy na nim nasza baza danych za pomoca obiektu Database z biblioteki JCouchDB. Po utworzeniu "rekordow" przelatujemy po nich petla for tworzac z nich tablice JSONow, by na koniec zaladowac ja do bazy na Couchu. Ogolnie jest to tzw. domkniecie (z ang. closure) */

def XML_To_CouchDB = { name_xml, name_couchdb, name_sqlite -> 

	def file = name_xml+".xml"
	def all = new File(file).text
	def quotes = new XmlSlurper().parseText(all) // parsowanie XMLa z baza danych
	def allQuotes = quotes.item // wybieranie znacznikow "item", ktore sa naszymi "rekordami" wrzucanymi do CouchDB

	def db = new Database("localhost", name_couchdb) // laczenie z lokalnym serwerem CouchDB 

	def json_map = []

	if (!db.getServer().createDatabase(db.getName())) // tworzenie bazy danych na serwerze
	{
		db.getServer().deleteDatabase(db.getName())
		db.getServer().createDatabase(db.getName())
	}

	for (q in allQuotes) Put_To_CouchDB(q, json_map) // tworzenie ostatecznego JSONA, jest to tablica wszystkich JSONow

	db.bulkCreateDocuments(json_map) // ladowanie tablicy JSONow do bazy na CouchDB
}

/* */

def CouchDB_To_SQLite = { name_xml, name_couchdb, name_sqlite  ->

	def sql = Sql.newInstance("jdbc:sqlite:./${name_sqlite}.db","org.sqlite.JDBC")

	try {
	   sql.execute("drop table QUOTES")
	} catch(Exception e){}

	println sql.execute('''create table QUOTES (
	    id integer not null primary key,
	    title varchar(300),
	    author varchar(300),
	    quote varchar(300),
	    subject varchar(300)
	)''')

	def co=0
	def json = ""
	def db = new Database("localhost", name_couchdb);

	ViewAndDocumentsResult<Object,BaseDocument> docs = db.query("_all_docs", Object.class, BaseDocument.class, null, null, null);
	
	json = "INSERT INTO QUOTES ('title', 'author', 'quote', 'subject')\n"
	
	def tmp = ""
	
	for (ValueAndDocumentRow<Object,BaseDocument> r : docs.getRows())
	{
		title = r.getDocument().getProperty("title") as String
		author = r.getDocument().getProperty("author") as String
		quote = r.getDocument().getProperty("quote") as String
		subject = r.getDocument().getProperty("subject") as String

		tmp += "select \"${title}\", \"${author}\", \"${quote}\", \"${subject}\"\n"
		tmp +=' union all '

		if (co %499 == 0 && co !=0)
		{
			tmp2 = tmp.substring(0, tmp.length() - 7)
			tmp2=tmp2 + ';'
			sql.execute(json+tmp2)
			tmp=""
			tmp2=""
		}

		co++
		println co
	}

	tmp2 = tmp.substring(0, tmp.length() - 7)
	tmp2 = tmp2 + ';'

	sql.execute(json+tmp2)
}

def SQLite_To_CouchDB = { name_xml, name_couchdb, name_sqlite ->
	
	def db = new Database("localhost", name_couchdb);
	def json_map = []

	if (!db.getServer().createDatabase(db.getName()))
	{
		db.getServer().deleteDatabase(db.getName())
		db.getServer().createDatabase(db.getName())
	}

	def sql = Sql.newInstance("jdbc:sqlite:./${name_sqlite}.db","org.sqlite.JDBC")
	sql.rows("select * from QUOTES" ).each { Put_To_CouchDB(it, json_map) }

	db.bulkCreateDocuments(json_map)
	println "koniec"
}

swing.edt {
	frame(title:'Aplikacja bazodanowa', size: [550,300], locationRelativeTo: null, defaultCloseOperation:JFrame.EXIT_ON_CLOSE, show:true) {
	lookAndFeel("system")
	vbox {
            	hbox {			

		def bx = { text, cou, num, Closure f -> 
	
			button(
			text: text,
			actionPerformed: {
		
				name_xml = textfield1.text
				name_couchdb = textfield2.text
				name_sqlite = textfield3.text

				println name_xml
				println name_couchdb
				println name_sqlite

				if (cou > 0)
				{
					kkk.add(label("Wgrywanie na ${text} juz dziala ..."))
					abc.revalidate()
				}
				else 
				{
					cou=1

					b0.setEnabled(false)
					b1.setEnabled(false)
					b2.setEnabled(false)

					if (num == 0) b0.setEnabled(true)
					if (num == 1) b1.setEnabled(true)
					if (num == 2) b2.setEnabled(true)
			
					doOutside() {
						kkk.add(label("Wgrywanie ${text} ..."))
						kkk.revalidate()
						
						f(name_xml, name_couchdb, name_sqlite)

						kkk.add(label("Zakonczono wgrywanie ${text} ..."))
						abc.revalidate()
		
						cou=0

						b0.setEnabled(true)
						b1.setEnabled(true)
						b2.setEnabled(true)
					}
				}}
			)
		}	

		b0 = bx("XML -> CouchDB", count1, 0, XML_To_CouchDB as Closure)
		b1 = bx("CouchDB -> SQLite", count2, 1, CouchDB_To_SQLite as Closure)
		b2 = bx("SQLite -> CouchDB", count3, 2, SQLite_To_CouchDB as Closure)

		b3 = button(
                	text: "Tabela SQLite",
                        actionPerformed: {

				name_xml = textfield1.text
				name_couchdb = textfield2.text
				name_sqlite = textfield3.text

                        	def sql = Sql.newInstance("jdbc:sqlite:./${name_sqlite}.db","org.sqlite.JDBC")

                                def dial = dialog (modal: true, size: [800,400]) {

                                scrollPane() {

                                myTable = swing.table()
                                {
                                	tableModel(){
                                        propertyColumn( header:"ID", propertyName:"idName", modelIndex: 0, maxWidth: 50 )
                                        propertyColumn( header:"Author", propertyName:"authorName" )
                                        propertyColumn( header:"Quote", propertyName:"quoteName" , modelIndex: 2, minWidth:400)
                                        propertyColumn( header:"Subject", propertyName:"subjectName" )
  		                        }
                        	}

				def rows
        	                sql.rows("select * from QUOTES" ).each {
                	        	rows = myTable.getModel().getRowsModel().getValue()
                        	        rows.add([idName: it.id, authorName: it.author, quoteName: it.quote, subjectName: it.subject])
	                        }
        	                myTable.getModel().getRowsModel().setValue(rows)
                	        }}

				dial.title = textfield3.text+".db"
                                dial.show()
                       }
		)}

		label(" ")

		pan1 = panel() {
			gridLayout(cols: 2, rows: 3)
			xml_label = label("dane w xml:")
			textfield1 = textField()	 
			textfield1.text="database" 
			couch_label = label("baza couchdb:")	 
			textfield2 = textField()	 
			textfield2.text = "quotes"
			sqlite_label = label("baza sqlite:")	 
			textfield3 = textField()	 
			textfield3.text = "test"	 
		}

		pan1.setMaximumSize(new Dimension(200,100))

		pan2 = panel() { textlabel = label("Informacje:") }
		pan2.setMaximumSize(new Dimension(550,10))

		abc = scrollPane(verticalScrollBarPolicy:JScrollPane.VERTICAL_SCROLLBAR_ALWAYS) { kkk = vbox {} }
        }}
}

