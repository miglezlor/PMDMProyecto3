package com.miglezlor.pmdmproyecto3;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    JSONObject json, cliente;
    public static String miNombre;


    private WebSocketClient mWebSocketClient;

    private static final int MY_PERMISSIONS_REQUEST_INTERNET=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //llamo al metodo instrucciones al iniciarse la aplicacion
        instrucciones();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Uso el boton de la plantilla para enviar el mensaje
                sendMessage();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();


        //Codigo para la insercion del nombre, pulsando en el boton de la barra lateral, se abre un dialogo de alerta, pidiendo
        //el nombre del usuario, si es distinto de null, se llama al metodo connectWebSocket()
        if (id == R.id.setName) {
            AlertDialog.Builder alert= new AlertDialog.Builder(this);
            final EditText user=new EditText(this);
            user.setSingleLine();
            user.setPadding(50,0,50,0);
            alert.setTitle("NickName");
            alert.setMessage("Introducir NickName");
            alert.setView(user);
            alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    miNombre =user.getText().toString();
                    if(miNombre !=null){
                        connectWebSocket();
                    }
                }
            });
            alert.setNegativeButton("Cancelar",null);
            alert.create();
            alert.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void connectWebSocket() {

        URI uri;
        try {
            uri = new URI("ws://chatjson-miglezlor.c9users.io:8081");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        Map<String, String> headers = new HashMap<>();

        mWebSocketClient = new WebSocketClient(uri, new Draft_17(), headers, 0) {

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("{\"id\":\"" + miNombre + "\"}");
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = (TextView)findViewById(R.id.textmensaje);
                        String nombre;
                        String mensaje;
                        String destinatario;
                        Boolean privacidad;
                        try {
                            cliente = new JSONObject(message);

                            //Lee y recoge los valores de los campos del json cliente
                            nombre= cliente.getString("id");
                            mensaje = cliente.getString("mensaje");
                            destinatario= cliente.getString("destino");
                            privacidad= cliente.getBoolean("Privado");

                            //Si la privacidad esta marcada y si hay texto en el campo de destinatario,
                            //solo lo vera la persona con el mismo nombre, en caso contrario leera "Mensaje Privado"
                            //Si la privacidad esta desmarcada, todos leeran el mensaje
                            if(privacidad.equals(Boolean.TRUE) & !destinatario.equals("")){
                                if(destinatario.equals(miNombre)){
                                    textView.setText(textView.getText() + "\n" + nombre+ "\n" + mensaje);
                                }
                                else
                                    textView.setText("Mensaje Privado");
                            }
                            else{
                                textView.setText(textView.getText() + "\n" + nombre + "\n" + mensaje);
                            }

                        }
                        catch(JSONException e){

                            textView.setText(textView.getText() + "\n" + message);
                        }
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };

        mWebSocketClient.connect();

    }
    public void sendMessage() {

        EditText mensaje = (EditText)findViewById(R.id.mensaje);
        CheckBox privacidad = (CheckBox)findViewById(R.id.privado);
        EditText destinatario = (EditText)findViewById(R.id.destinatario);


        Boolean privac;

        if(privacidad.isChecked()) {
            privac = Boolean.TRUE ;
        }else{
            privac = Boolean.FALSE;
        }
        json = new JSONObject();
        try {
            //Añado los valores al objeto json creado
            json.put("id", miNombre);
            json.put("mensaje",mensaje.getText().toString());
            json.put("destino",destinatario.getText().toString());
            json.put("Privado",privac);
            mensaje.setText("");
            destinatario.setText("");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Envio el json para la escritura
        mWebSocketClient.send(json.toString());


    }

    //Metodo formado por un dialogo de alerta con las instrucciones basicas de la app
    public void instrucciones() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setTitle("Conexión");
        build.setMessage(instruccioes);
        build.setPositiveButton("Aceptar", null);
        build.create();
        build.show();
    }
    public String instruccioes = "Pasos para comenzar el chat: \n" +
            "1-Elige tu nombre a traves del boton de la barra lateral.\n" +
            "2-Aceptar y la conexión se establecera automaticamente";
}
