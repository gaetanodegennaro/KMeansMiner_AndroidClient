package degennarogaetano.kmm_client;


import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Attività creata per mostrare la risposta del server dopo l'invio della richiesta.
 * <br />
 * Nel caso sia stata richiesta la scoperta dei cluster a partire dalle informazioni presenti nella base di dati
 * viene creato un grafico a torta, suddiviso in k parti (numero di centroidi), le cui aree sono proporzionali al
 * numero di tuple appartenenti a quel cluster.
 * Viene mostrata anche una legenda con i centroidi, ciascuno seguito dalle tuple appartenenti al proprio cluster.
 * Segue infine la stringa "non lavorata", ottenuta come risposta dal server
 * <br />
 * Nel caso sia stata richiesta la scoperta dei cluster a partire da file viene solo mostrata la stringa "non lavorata"
 * ottenuta come risposta dal server.
 * Questo perchè non si dispone di abbastanza informazioni per la creazione del grafico.
 *
 */
public class ResultActivity extends AppCompatActivity
{

    /**
     * Avviato quando l'attività viene avviata.
     *
     * @param savedInstanceState un mapping di parametri che possono essere forniti in input all'Activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
    }


    /**
     * {@link Fragment}  dell'Activity.
     */
    public static class ResultFragment extends Fragment
    {
        public ResultFragment(){}

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
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_result, container, false);

            HashMap<String, ArrayList<String>> kMeansResult = splitResult(getActivity().getIntent().getExtras().getString("result"));

            if(getActivity().getIntent().getExtras().getInt("resultType")==0)
            {
                String legend = "";
                ArrayList<Integer> colors = new ArrayList();
                for(String centroid: kMeansResult.keySet())
                {
                    Random rnd = new Random();
                    int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    colors.add(color);

                    legend+="<font color='"+color+"'>  &#9607;  </font><font color='black' size='10px'>"+centroid+"</font><br />";
                    ArrayList<String> tuples = kMeansResult.get(centroid);
                    for(String tuple: tuples)
                    {
                        legend+="<span>&nbsp;&nbsp;&nbsp;&nbsp;"+tuple+"</span><br />";
                    }
                }

                TextView textLegend = (TextView) rootView.findViewById(R.id.legend);
                textLegend.setText(Html.fromHtml(legend), TextView.BufferType.SPANNABLE);

                PieChart chart = (PieChart) rootView.findViewById(R.id.chart);
                chart.setDrawEntryLabels(true);
                addDataSet(chart, kMeansResult, colors);
                chart.setRotationEnabled(false);
                chart.setTouchEnabled(true);
                chart.setDragDecelerationEnabled(true);
                chart.enableScroll();
                chart.getDescription().setEnabled(false);
            }
            else
            {
                ((PieChart) rootView.findViewById(R.id.chart)).setVisibility(View.GONE);
            }

            TextView textRetriviedData = (TextView) rootView.findViewById(R.id.retriviedData);
            textRetriviedData.setText("RECEIVED DATA FROM SERVER:\n\n"+getActivity().getIntent().getExtras().getString("result"));

            return rootView;
        }

        /**
         * Si occupa di avvalorare il grafico.
         *
         * @param chart grafico da avvalorare
         * @param kMeansResult HashMap composto da coppie (centroide, tuple appartenenti al centroide)
         * @param colors Colori generati in maniera casuale da attribuire ad ogni parte del grafico
         */
        private void addDataSet(PieChart chart, HashMap<String, ArrayList<String>> kMeansResult, ArrayList<Integer> colors)
        {
            ArrayList<PieEntry> yEntries = new ArrayList();
            ArrayList<String> xEntries = new ArrayList();

            int i=0;
            for(String centroid : kMeansResult.keySet())
            {
                yEntries.add(new PieEntry((float)kMeansResult.get(centroid).size(), i));
                i++;
            }

            for(String centroid : kMeansResult.keySet())
            {
                xEntries.add(centroid);
            }


            PieDataSet pieDataSet = new PieDataSet(yEntries, "");
            pieDataSet.setSliceSpace(2);
            pieDataSet.setValueTextSize(12);
            pieDataSet.setColors(colors);

            chart.getLegend().setEnabled(false);
            chart.setData(new PieData(pieDataSet));
            chart.invalidate();
        }

        /**
         * Si occupa di creare un HashMap che contiene in maniera ordinata i risultati dell'attività di scoperta dei cluster.
         * L'HashMap è composto da coppie (centroide, tuple appartenenti al centroide).
         *
         * @param result Stringa contenente il risultato della richiesta al server
         * @return HashMap composto da coppie (centroide, tuple appartenenti al centroide).
         */
        private HashMap<String, ArrayList<String>> splitResult(String result)
        {
            HashMap<String, ArrayList<String>> kMeansResult = new HashMap();
            String firstSplitResult[] = result.split("\\(");
            for(int i=1; i<firstSplitResult.length;i++)
            {
                String secondSplitResult[] = firstSplitResult[i].split("\\)");
                String centroid = secondSplitResult[0];

                ArrayList<String> values = new ArrayList();
                for(int j=0; j<secondSplitResult.length; j++)
                {
                    String thirdSplitResult[] = secondSplitResult[j].split("\\[");
                    for(int k=1; k<thirdSplitResult.length; k++)
                    {
                        values.add(thirdSplitResult[k].split("\\]")[0]);
                    }
                }
                kMeansResult.put(centroid, values);
            }
            return kMeansResult;
        }
    }
}
