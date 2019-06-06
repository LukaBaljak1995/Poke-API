package com.example.lukabaljak.pokeapi;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    // Sva polja će biti tipa TextView, iako je pokemonName zapravo EditText.
    TextView  pokemonName, textViewHeight, textViewWeight, textViewType, textViewProgress;

    ProgressBar progressBar;

    ImageView pokeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pokemonName=findViewById(R.id.pokemonName);
        textViewHeight = findViewById(R.id.textViewHeight);
        textViewWeight = findViewById(R.id.textViewWeight);
        textViewType = findViewById(R.id.textViewType);
        textViewProgress = findViewById(R.id.textViewProgress);
        pokeImage = findViewById(R.id.pokeImage);
        progressBar = findViewById(R.id.progressBar);

        //U sliku sa id-em pokeImage ce prvo biti ucitan placeholder koji se nalazi
        //na odredjenoj lokaciji na Webu.
        Picasso.get().load("https://i0.wp.com/hifadhiafrica.org/wp-content/uploads/2017/01/default-placeholder.png").into(pokeImage);

    }


    public void Display(View view) {
        textViewProgress.setVisibility(View.VISIBLE);
        //Uzima se vrednost koju korisnik unese.
        String name = String.valueOf(pokemonName.getText());

        //Instancira se klasa GetPokemon i metodom execute, kojoj se prosledjuje
        //uneto ime pokemona, zapocinje se izvrsavanje komunikacije sa API-em.
        GetPokemon getPokemon = new GetPokemon();
        getPokemon.execute(name);

    }


    //String - parametri metode doInBackroug
    //Integer - parametri metode onProgressUpdate
    //JSONObject - parametar metode onPostExecute
    private class GetPokemon extends AsyncTask<String, Integer, JSONObject>{


        //strings je zapravo niz Stringova.
        @Override
        protected JSONObject doInBackground(String... strings) {

            //Prvi string (u ovom slucaju i jedini) predstavlja ime pokemona.
            String name = strings[0];
            JSONObject json;
            try {

                //URL koji vodi do api-a
                URL url = new URL("https://pokeapi.co/api/v2/pokemon/"+name+"/");
                //konekcija sa URL-om
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //Posto se salje HTTP zahtev, potrebno je postaviti metod.
                //U ovom slucaju to je GET.
                connection.setRequestMethod("GET");

                //Pozivom metode publishProgress(10) okida se metoda
                //onProgressUpdate(10) i prikazuje se korisniku progres.
                publishProgress(10);

                //Uz pomoc BufferedReader-a citamo podatke iz API-a i smestamo u response.
                BufferedReader bufferedReader = new BufferedReader
                        (new InputStreamReader(connection.getInputStream()));

                String response = "";

                publishProgress(40);
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    String line;
                    while((line=bufferedReader.readLine())!=null){
                        response+=line;
                    }
                    Log.d("JSONRESPONSE",response);
                    //Od stringa response pravimo JSON objekat.
                    json = new JSONObject(response);
                    publishProgress(80);
                    // Ovaj json je ulaz u metodu onPostExecute()
                    return  json;
                }



            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            /* Metoda se okida pozivanjem metode publishProgress(int) u metodama
               doInBackground i onPostExecute. Uzima se vrednost progresa i ispisuje
               u odgovarajucem polju grafickog interfejsa.*/

            int progress = values[0];
            Log.d("PROGRESVALUE",values[0]+"");
            String progressText = "Progress: " +progress+"%!";
            textViewProgress.setText(progressText);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(progress);
            if(progress==100){
                progressBar.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            if(jsonObject!=null){

                try {

                    // Unutar slike pokeImage smestamo sliku pokemona. Slike se ne salju preko API-a,
                    // vec se nalaze negde na internetu.
                    Picasso.get().load("https://img.pokemondb.net/artwork/"+jsonObject.getString("name")+".jpg").into(pokeImage);

                    //Uzima se vrednost polja height JSON objekta o pokemonu i prikazuje.
                    double height = jsonObject.getDouble("height");
                    textViewHeight.setText(height/10 +"m");

                    //Uzima se vrednost polja weight JSON objekta o pokemonu i prikazuje.
                    double weight = jsonObject.getDouble("weight");
                    textViewWeight.setText(weight/10 + "kg");

                    publishProgress(95);

                    /*  Sto se polja types tice, on predstavlja niz, pa je potrebno
                        parsirati ga na odgovarajuci nacin preko JSONArray objekta. */
                    JSONArray js = new JSONArray(jsonObject.getString("types"));
                    String pokeTypes="";
                    for (int i =0; i< js.length(); i++) {
                        JSONObject jss = js.getJSONObject(i);
                        JSONObject type = new JSONObject(jss.getString("type"));
                        if(i==0){
                            pokeTypes=type.getString("name");
                        } else {
                            pokeTypes+= ", "+type.getString("name");
                        }
                    }
                    textViewType.setText(pokeTypes);

                    publishProgress(100);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else{
                //Ukoliko ne postoji pokemon sa unetim imenom, ispisuje se poruka o tome.
                Toast.makeText(MainActivity.this, "Ne postoji taj pokemon!", Toast.LENGTH_SHORT).show();
                publishProgress(100);
            }

            super.onPostExecute(jsonObject);
            //Polje za progres se sakriva, jer je operacija zavrsena.
            textViewProgress.setVisibility(View.INVISIBLE);
        }
    }




}
