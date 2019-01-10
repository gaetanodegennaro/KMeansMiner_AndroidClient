package degennarogaetano.kmm_client;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Attività all'interno della quale si svolgono tutte le operazioni di richieste al server.
 * Attività composta da due {@link TabbedFragment}.
 * Uno per gestire le richieste per la scoperta di cluster a partire dalla base di dati,
 * l'altro per gestire le richeiste per la scoperta di cluster a partire da file.
 */
public class KMeansActivity extends AppCompatActivity
{
    /**
     * Il {@link android.support.v4.view.PagerAdapter} che fornisce i
     * fragments per ogni sezione. Si utilizza una sottoclasse di
     * {@link FragmentPagerAdapter}, che terrà ogni fragment caricato in memoria.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * Il {@link ViewPager} che conterrà il contenuto delle sezioni.
     */
    private ViewPager mViewPager;

    /**
     * Stream di output che permette di inviare richieste al server.
     */
    private ObjectOutputStream out;

    /**
     * Stream di input che permette di ricevere informazioni dal server.
     */
    private ObjectInputStream in;

    private Socket socket;

    /**
     * Consente di non tornare indietro alla {@link MainActivity}
     */
    @Override
    public void onBackPressed() {

    }

    /**
     * Consente di chiudere gli streem quando l'attività viene distrutta e non sarà più utilizzabile.
     */
    @Override
    public void onDestroy()
    {
        try
        {
            in.close();
            out.close();
        }
        catch(IOException e){}
        super.onDestroy();
    }

    /**
     * Avviato quando l'attività viene avviata.
     * Successivamente invoca {@link #initConnection()}
     * @param savedInstanceState un mapping di parametri che possono essere forniti in input all'Activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kmeans);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        initConnection();
    }

    /**
     * Inizializza la connessione con il server.
     * Apre lo stream di input {@link #in} e lo stream di output {@link #out}.
     * Tutte le operazioni di rete vanno eseguite in un thread diverso da quello principale.
     * Un nuovo thread viene pertanto lanciato.
     *
     * !!!!!!!!NB: a causa di un bug: https://issuetracker.google.com/issues/36912723
     * non viene lanciata nessuna eccezione se non è possibile connettersi al server
     * TESTATO CON JAVA 7 E JAVA 8!!!!!!!!
     */
    private void initConnection()
    {
        final KMeansActivity activity = this;
        Thread threadSocketCreator = new Thread()
        {
            public void run()
            {
                Intent intent = getIntent();
                try
                {
                    InetAddress addr = InetAddress.getByName(intent.getStringExtra("ip"));
                    socket = new Socket(addr, intent.getIntExtra("port",0)); //Port
                    out = new ObjectOutputStream(socket.getOutputStream());
                    in = new ObjectInputStream(socket.getInputStream());
                }
                catch(IOException e)
                {
                    System.out.println("OK");
                    /*Non è possibile creare AlertDialog in un Thread che non sia quello in cui viene eseguita l'attività.
                      Si utilizza quindi runOnUIThread per eseguire il contenuto sul Thread dell'User Interface.
                     */
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            AlertDialog.Builder builder;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
                            }
                            else builder = new AlertDialog.Builder(activity);

                            builder.setTitle("Connection error.")
                                    .setMessage("Unable to connect to server.")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int which) {
                                            System.exit(0);
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    });
                }
            }
        };
        threadSocketCreator.start();
        //try{threadSocketCreator.join();}catch(InterruptedException e){e.printStackTrace();}
    }


    /**
     * {@link Fragment} dell'Activity.
     * Per questa Activity verranno istanziati due TabbedFragment.
     * Uno per gestire le richieste per la scoperta di cluster a partire dalla base di dati,
     * l'altro per gestire le richeiste per la scoperta di cluster a partire da file.
     */
    public static class TabbedFragment extends Fragment
    {
        /**
         * EditText atta a contener il numero k di Cluster che si intende scoprire.
         */
        private EditText kEditText;

        /**
         * Nome della tabella dalla quale reperire le informazioni dal database.
         */
        private EditText tableNameEditText;

        /**
         * TextView atta al display di alcuni stati di errore.
         */
        private TextView errorHandler;

        /**
         * Ascoltatore che ordinerà le operazioni da eseguire quando il pulsante del Fragment viene premuto.
         */
        private View.OnClickListener listener;

        public TabbedFragment(){}

        /**
         * Restituisce un riferimento a {@link #kEditText}
         * @return {@link #kEditText}
         */
        EditText getKEditText(){return kEditText;}

        /**
         * Restituisce un riferimento a {@link #tableNameEditText}
         * @return {@link #tableNameEditText}
         */
        EditText gettableNameEditText(){return tableNameEditText;}

        /**
         * Imposta il testo di {@link #errorHandler}
         * @param message messaggio da impostare
         */
        void setError(String message)
        {
            errorHandler.setText(message);
        }

        /**
         * Set di {@link #listener}.
         * @param listener Oggetto OnClickListener da assegnare a {@link #listener}.
         */
        void setListener(View.OnClickListener listener){this.listener = listener;}

        /**
         * Restituisce una nuova istanza di questo fragment per il dato sectionNumber.
         */
        TabbedFragment newInstance(int sectionNumber) {
            TabbedFragment fragment = new TabbedFragment();
            Bundle args = new Bundle();
            args.putInt("section_number", sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        /**
         * Avviato quando il Fragment viene creato.
         *
         * @param inflater Crea un'istanza di un file XML di layout negli oggetti View corrispondenti.
         * @param container E' una vista speciale che può contenere altre viste (chiamate child).
         *                  Il gruppo di viste è la classe base per i contenitori di layout e viste.
         * @param savedInstanceState un mapping di parametri che possono essere forniti in input al Fragment.
         * @return View creata
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_kmeans, container, false);

            Button loadButton = (Button) rootView.findViewById(R.id.loadButton);
            //loadButton.setOnClickListener(listener);

            loadButton.setOnClickListener(listener);

            kEditText = (EditText) rootView.findViewById(R.id.clustersNumber);
            tableNameEditText = (EditText) rootView.findViewById(R.id.tableName);
            errorHandler = (TextView) rootView.findViewById(R.id.errorHandler);

            return rootView;
        }

    }

    /**
     * Sottoclasse di {@link FragmentPagerAdapter} che restituisce un fragment corrispondente ad una delle sezioni/tab
     */
    class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        /**
         * TabbedFragment del primo tab per gestire le richieste per la scoperta di cluster a partire dalla base di dati.
         */
        private TabbedFragment dbFragment;

        /**
         * TabbedFragment del secondo tab per gestire le richieste per la scoperta di cluster a partire da file.
         */
        private TabbedFragment fileFragment;

        /**
         * Richiama il costruttore della superclasse.
         * @param fm interfaccia per interagire con oggetti Fragment in una Activity
         */
        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Restituisce il fragment in base al tab selezionato, identificato dalla posizione position.
         * In base al tab, si occupa di creare un OnClickListener per il Button dell'ascoltatore,
         * in modo da poter effettuare operazioni diverse per ogni tab e gestire le due richieste differenti.
         *
         * @param position posizione del tab selezionato
         * @return Restituisce il fragment dell'Activity in base al tab selezionato, identificato da position
         */
        @Override
        public Fragment getItem(int position)
        {
            switch(position)
            {
                case 0:
                {
                    dbFragment = new TabbedFragment();
                    dbFragment.setListener(new View.OnClickListener()
                    {
                        public void onClick(View view)
                        {
                            Thread thread = new Thread()
                            {
                                public void run()
                                {
                                    try
                                    {
                                        String result = learningFromDb(dbFragment.gettableNameEditText().getText().toString(),
                                                dbFragment.getKEditText().getText().toString());

                                        errorHandler(dbFragment, "");
                                        Intent intent = new Intent(dbFragment.getActivity(), ResultActivity.class);
                                        intent.putExtra("resultType", 0);
                                        intent.putExtra("result", result);
                                        startActivity(intent);
                                    }
                                    catch(ClassNotFoundException e){errorHandler(dbFragment, e.getMessage());}
                                    catch(IOException e){errorHandler(dbFragment, e.getMessage());}
                                    catch(ServerException e) {errorHandler(dbFragment, e.getMessage());}
                                    catch(NumberFormatException e){errorHandler(dbFragment, e.getMessage());}
                                }
                            };
                            thread.start();
                        }
                    });
                    return dbFragment;
                }
                case 1:
                {
                    fileFragment = new TabbedFragment();
                    fileFragment.setListener(new View.OnClickListener()
                    {
                        public void onClick(View view)
                        {
                            Thread thread = new Thread()
                            {
                                public void run()
                                {
                                    try
                                    {
                                        String result = learningFromFile(fileFragment.gettableNameEditText().getText().toString(),
                                                fileFragment.getKEditText().getText().toString());

                                        errorHandler(fileFragment, "");
                                        Intent intent = new Intent(fileFragment.getActivity(), ResultActivity.class);
                                        intent.putExtra("resultType", 1);
                                        intent.putExtra("result", result);
                                        startActivity(intent);
                                    }
                                    catch(ClassNotFoundException e){errorHandler(fileFragment, e.getMessage());}
                                    catch(IOException e){errorHandler(fileFragment, e.getMessage());}
                                    catch(ServerException e){errorHandler(fileFragment, e.getMessage());}
                                    catch(NumberFormatException e){errorHandler(fileFragment, e.getMessage());}
                                }
                            };
                            thread.start();
                        }
                    });
                    return fileFragment;
                }
                default: return null;
            }
        }

        /**
         * Si occupa di far eseguire sul thread principale una modifica dell'interfaccia. (Modifiche dell'interfaccia
         * possono essere eseguite solo sul thread principale)
         * Nel dettaglio, si occupa di cambiare il testo della TextView che mostra eventuali messaggi di errore.
         *
         * @param fragment TabbedFragment all'interno del quale si desidera eseguire l'operazione
         * @param message Messaggio da settare per la TextView
         */
        private void errorHandler(final TabbedFragment fragment, final String message)
        {
            /*Solo il thread che crea la UI può modificare i suoi componenti.*/
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    fragment.setError(message);
                }
            });
        }

        /**
         * Restituisce il numero totale di tab
         * @return numero di tab presenti
         */
        @Override
        public int getCount() {
            return 2;
        }

        /**
         * Utilizzato per la scoperta dei cluster a partire dalle informazioni presenti nella base di dati.
         * Si avvale degli input forniti dall'utente nelle rispettive TextView per ottenere il nome della tabella
         * dalla quale attingere informazioni e il numero k di cluster.
         * Avviene un controllo client-side sull'input di k, che impedisce l'inserimento di numeri negativi.
         *
         * @param tableName Nome della tabella dalla quale attingere informazioni
         * @param clustersNumber Numero di cluster che si intende scoprire
         * @return Risposta del server dopo l'esecuzione della richiesta.
         * @throws IOException sollevata quando si verificano errori durante la lettura/scrittura di informazioni da/su server mediante gli stream
         * @throws ClassNotFoundException sollevata quando si effettua il cast ad un tipo non risolvibile
         * @throws ServerException sollevata quando su server si verifica un'eccezione grazie alla quale non è possibile portare a termine la richiesta
         */
        private String learningFromDb(String tableName, String clustersNumber) throws IOException, ClassNotFoundException, ServerException, NumberFormatException
        {
            String computationResult = "";
            int k = new Integer(clustersNumber).intValue();

            if(k<1) throw new NumberFormatException("Value not allowed for k.");

            out.writeObject(0);
            out.writeObject(tableName);
            String result = (String)in.readObject();
            if(!result.equals("OK")) throw new ServerException(result);

            out.writeObject(1);
            out.writeObject(k);
            result = (String)in.readObject();
            if(!result.equals("OK")) throw new ServerException(result);

            computationResult = "Iterations number: "+in.readObject()+"\n"+(String)in.readObject()+"\n";

            out.writeObject(2);

            result = (String)in.readObject();
            if(!result.equals("OK")) throw new ServerException(result);
            else computationResult+="Operation completed successfully!";

            return computationResult;
        }

        /**
         * Utilizzato per la la lettura di un file presente su server.
         * Si avvale degli input forniti dall'utente nelle rispettive caselle di testo per ottenere (mediante concatenazione)
         * il nome del file dal quale effettuare l'operazione di lettura.
         *
         * @param tableName Nome della tabella dalla quale attingere informazioni
         * @param clustersNumber Numero di cluster che si intende scoprire
         * @return Risposta del server dopo l'esecuzione della richiesta.
         * @throws IOException sollevata quando si verificano errori durante la lettura/scrittura di informazioni da/su server
         * @throws ClassNotFoundException sollevata quando si effettua il cast ad un tipo non risolvibile
         * @throws ServerException sollevata quando su server si verifica un'eccezione grazie alla quale non è possibile portare a termine la richiesta
         */
        private String  learningFromFile(String tableName, String clustersNumber) throws IOException, ClassNotFoundException, ServerException, NumberFormatException
        {
            int k = new Integer(clustersNumber).intValue();
            if(k<1)throw new NumberFormatException("Value not allowed for k.");

            out.writeObject(3);
            out.writeObject(tableName);

            out.writeObject(Integer.parseInt(clustersNumber));
            String result = (String)in.readObject();

            if(!result.equals("OK")) throw new ServerException(result);

            result = (String) in.readObject();
            result+="\nOperation completed successfully!";

            return result;
        }
    }
}
