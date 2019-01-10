package degennarogaetano.kmm_client;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Attività principale dell'applicazione.
 * Possiede un semplice fragment che richiede l'inserimento dell'indirizzo del server.
 * Se quest'ultimo è inserito in maniera corretta, {@link KMeansActivity} viene lanciata.
 */
public class MainActivity extends AppCompatActivity
{
    /**
     * Avviato quando l'attività viene avviata.
     * @param savedInstanceState un mapping di parametri che possono essere forniti in input all'Activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    /**
     * {@link Fragment} dell'Activity.
     */
    /*Obbligato ad essere public*/
    public static class RequestAddressFragment extends Fragment
    {
        /*Obbligato ad essere public*/
        public RequestAddressFragment(){}

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
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            Button openButton = (Button) rootView.findViewById(R.id.openButton);
            openButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    EditText editText = (EditText) rootView.findViewById(R.id.IPAddress);
                    String splittedAddress[] = editText.getText().toString().split(":");

                    TextView errorHandler = (TextView) rootView.findViewById(R.id.errorHandler);

                    if(splittedAddress.length!=2 || splittedAddress[0].equals("") || splittedAddress[1].equals(""))
                    {
                        errorHandler.setText("Insert address in <IP : PORT> format!");
                    }
                    else
                    {
                        int port = 0;
                        try
                        {
                            port = Integer.parseInt(splittedAddress[1]);
                            if(port>65535) throw new NumberFormatException();
                        }
                        catch(NumberFormatException e){errorHandler.setText("Illegal port value!"); return;}
                        Intent intent = new Intent(getActivity(), KMeansActivity.class);
                        intent.putExtra("ip",splittedAddress[0]);
                        intent.putExtra("port",port);
                        startActivity(intent);
                    }
                }
            });
            return rootView;
        }
    }
}

