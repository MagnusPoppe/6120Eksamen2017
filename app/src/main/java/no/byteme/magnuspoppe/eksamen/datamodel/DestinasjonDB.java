package no.byteme.magnuspoppe.eksamen.datamodel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database adapter klasse for destinasjon objekter.
 *
 * Klassen er laget ved å følge mønstre i prosjektet fra læreboken litt: "hour16application".
 * Finnes på Github: https://github.com/CarmenDelessio/Hour16Application
 *
 * Og veldig mye bruk av denne treningsartikkelen:
 * https://developer.android.com/training/basics/data-storage/databases.html
 *
 * Created by MagnusPoppe on 31/05/2017.
 */
public class DestinasjonDB
{

    private static final String DATABASE_NAVN = "db210852.db";
    private static final String TABELL_NAVN = "location";
    private static final int VERSJON = 2;
    private final Context context;
    public static String TAG = DestinasjonDB.class.getSimpleName();

    // NAVN PÅ KOLONNER:
    final static private String KOL_ID  = "locationID";
    final static private String KOL_EIER = "owner";
    final static private String KOL_NAVN = "name";
    final static private String KOL_TYPE = "type";
    final static private String KOL_BESKRIVELSE = "description";
    final static private String KOL_BILDEURL = "ImageURL";
    final static private String KOL_LAT = "lat";
    final static private String KOL_LNG = "lng";
    final static private String KOL_MOH = "moh";
    final static private String KOL_GLOBAL = "isGlobal";

    private DatabaseHelper dbHjelp;
    SQLiteDatabase DB;

    /**
     * Konstruktør
     * @param context
     */
    public DestinasjonDB(Context context)
    {
        this.context = context;
    }

    /**
     * Åpner forbindelse med SQLite database:
     * @return
     * @throws SQLException
     */
    public DestinasjonDB open() throws SQLException
    {
        dbHjelp = new DatabaseHelper(context);
        DB = dbHjelp.getWritableDatabase();
        return this;
    }

    /**
     * Lukker forbindelse med SQLite database:
     */
    public void close()
    {
        if (dbHjelp != null)
        {
            dbHjelp.close();
        }
    }

    /**
     * Oppgraderer databasen om skriptet er endret. Siden
     * denne databasen kun holder på temp data har jeg ikke laget
     * noen overføringsmetode.
     * @throws SQLException
     */
    public void upgrade() throws SQLException
    {
        dbHjelp = new DatabaseHelper(context); //open
        DB = dbHjelp.getWritableDatabase();
        dbHjelp.onUpgrade(DB, 1, 0);
    }

    /**
     * Lager en insettingssetning og setter inn i databasen.
     * @param destinasjon objeketet som skal lagres.
     */
    public void insertDestinasjon(Destinasjon destinasjon)
    {
        // Lagger insert setning:
        ContentValues verdier = new ContentValues();
        verdier.put(KOL_EIER, destinasjon.getEier());
        verdier.put(KOL_NAVN, destinasjon.getNavn());
        verdier.put(KOL_TYPE, destinasjon.getType());
        verdier.put(KOL_BESKRIVELSE, destinasjon.getBeskrivelse());
        verdier.put(KOL_BILDEURL, destinasjon.getBildeURL());
        verdier.put(KOL_LAT, destinasjon.getKoordinat().latitude);
        verdier.put(KOL_LNG, destinasjon.getKoordinat().longitude);
        verdier.put(KOL_MOH, destinasjon.getMoh());
        verdier.put(KOL_GLOBAL, destinasjon.isiGlobalDatabase());

        // Setter inn:
        destinasjon.setDatabaseID((int) DB.insert(TABELL_NAVN, null, verdier));
    }

    /**
     * Sletter mange oppføringer i databasen sekvensielt.
     * @param ider på oppføringer, tilsvarer kolonnen "locationID
     */
    public void deleteDestinasjon(int[] ider)
    {
        for (int id: ider)
            deleteDestinasjon(id);
    }

    /**
     * Sletter en enkel oppføring i databasen hvis oppføringen finnes.
     * @param id på oppføring, tilsvarer kolonnen "locationID
     */
    public void deleteDestinasjon(Integer id)
    {
        // Spesifiserer SELECT og WHERE:
        String selection =  KOL_ID + " = ?";
        String[] selectionArgs = { id.toString() };

        // Utfører spørring:
        DB.delete(TABELL_NAVN, selection, selectionArgs);
    }

    /**
     * Henter ut alle destinasjoner lagret i databasen og pakker
     * dem inn i objekter.
     * @return Tabell med destinasjonsobjekter eller NULL hvis databasen er tom.
     */
    public Destinasjon[] getAlleDestinasjoner()
    {
        SQLiteDatabase db = dbHjelp.getReadableDatabase();

        // Definerer kolonner jeg ønsker utog annet viktig:
        String[] resultatKolonner = {
                KOL_ID,                                                      // INT ID
                KOL_EIER, KOL_NAVN, KOL_TYPE, KOL_BESKRIVELSE, KOL_BILDEURL, // Streng kolonner
                KOL_LAT, KOL_LNG, KOL_MOH, KOL_GLOBAL                        // Andre verdier
        };
        String whereSetning = null;
        String[] whereArgumenter = null;
        String gruppering = null;
        String havingSetning = null;
        String sortering = null;

        // Utfører spørring:
        Cursor cursor = db.query(
                TABELL_NAVN,
                resultatKolonner,
                whereSetning,
                whereArgumenter,
                gruppering,
                havingSetning,
                sortering
        );

        return spørringDestinasjoner(cursor);
    }

    /**
     * Henter ut alle destinasjoner lagret i databasen og pakker
     * dem inn i objekter.
     * @return Tabell med destinasjonsobjekter eller NULL hvis databasen er tom.
     */
    public Destinasjon[] getAlleUopplastedeDestinasjoner()
    {
        SQLiteDatabase db = dbHjelp.getReadableDatabase();

        // Definerer kolonner jeg ønsker ut og annet viktig:
        String[] resultatKolonner = {
                KOL_ID,                                                      // INT ID
                KOL_EIER, KOL_NAVN, KOL_TYPE, KOL_BESKRIVELSE, KOL_BILDEURL, // Streng kolonner
                KOL_LAT, KOL_LNG, KOL_MOH, KOL_GLOBAL                        // Andre verdier
        };
        String whereSetning = KOL_GLOBAL + " = 0";
        String[] whereArgumenter = null;
        String gruppering = null;
        String havingSetning = null;
        String sortering = null;

        // Utfører spørring:
        Cursor cursor = db.query(
                TABELL_NAVN,
                resultatKolonner,
                whereSetning,
                whereArgumenter,
                gruppering,
                havingSetning,
                sortering
        );

        return spørringDestinasjoner(cursor);
    }

    /**
     * Utfører en spørring for en ferdigkonfigurert cursor. Denne er den
     * generelle spørringsmetoden for "location" tabellen.
     * @param cursor Ferdig konfigurert for spørringen.
     * @return Resultat, eller null hvis ingen resultat.
     */
    private Destinasjon[] spørringDestinasjoner(Cursor cursor)
    {
        int antallOppføringer = cursor.getCount();

        // Ingen oppføringer i database:
        if (antallOppføringer < 1)
            return null;

        Destinasjon[] destinasjoner = new Destinasjon[antallOppføringer];

        int i = 0;
        // Henter ut data og gjør det om til objekter:
        while (cursor.moveToNext())
        {
            destinasjoner[i] = new Destinasjon(
                    cursor.getInt(0),       // ID
                    cursor.getString(1),    // Eier
                    cursor.getString(2),    // Navn
                    cursor.getString(3),    // Type
                    cursor.getString(4),    // Beskrivelse
                    cursor.getString(5),    // Bilde URL
                    cursor.getDouble(6),    // Latitude
                    cursor.getDouble(7),    // Longitude
                    cursor.getInt(8)        // Meter over havet (MOH)
            );
            destinasjoner[i].setiGlobalDatabase(cursor.getInt(9) > 0);

            i++;
        }

        // Ferdigstiller spørring:
        cursor.close();
        return destinasjoner;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAVN, null, VERSJON);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_SQL);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABELL_NAVN);
            onCreate(db);
        }
    }

    /**
     * Create table skriptet mitt. Dette er et litt modifisert skript hentet
     * fra E/R diagrammet vedlagt. Enkelte syntaksendringer har skjedd, og
     * indekser måtte fjernes.
     */
    private static final String CREATE_TABLE_SQL = "CREATE TABLE `"+ TABELL_NAVN +"` (\n" +
            "  `locationID` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  `owner` VARCHAR(128) NOT NULL," +
            "  `name` VARCHAR(255) NULL,\n" +
            "  `type` VARCHAR(255) NULL,\n" +
            "  `description` VARCHAR(2048) NULL,\n" +
            "  `imageURL` VARCHAR(256) NULL,\n" +
            "  `lat` DOUBLE NULL,\n" +
            "  `lng` DOUBLE NULL,\n" +
            "  `moh` INT NULL,\n" +
            " `isGlobal` BOOLEAN"+
            ")\n";
}
