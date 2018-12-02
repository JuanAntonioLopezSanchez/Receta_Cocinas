package mx.edu.iitepic.damd_u4_practica1_recetas_cocinas;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText identificacion, nombre, ingredientes, preparacion, observaciones;
    Button insertar,consultar,eliminar,actualizar;
    BaseDatos base;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        identificacion = findViewById(R.id.Ident);
        nombre = findViewById(R.id.Nombre);
        ingredientes = findViewById(R.id.Ingredientes);
        preparacion = findViewById(R.id.Preparacion);
        observaciones = findViewById(R.id.Observaciones);

        insertar = findViewById(R.id.Insertar);
        consultar = findViewById(R.id.Consultar);
        eliminar = findViewById(R.id.Eliminar);
        actualizar = findViewById(R.id.Actualizar);

        base = new BaseDatos(this,"primera", null,1);

        insertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codigoInsertar();
            }
        });

        consultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedirID(1);
            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedirID(2);
            }
        });

        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actualizar.getText().toString().startsWith("CONFIRMAR ACTUALIZACION")){
                    invocarConfirmacionActualizacion();
                }else{
                    pedirID(3);
                }
            }
        });

    }

    private void invocarConfirmacionActualizacion() {

        AlertDialog.Builder confir = new AlertDialog.Builder(this);
        confir.setTitle("IMPORTNATE").setMessage("estas seguro que deseas aplicar cambios")
                .setPositiveButton("si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        aplicarActualizar();
                        dialog.dismiss();
                    }
                }).setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                habilitarBotonesYLimpiarCampos();
                dialog.cancel();
            }
        }).show();

    }

    private void aplicarActualizar() {

        try{
            SQLiteDatabase tabla = base.getWritableDatabase();

            String SQL= "UPDATE RECETA SET NOMBRE='" +nombre.getText().toString() +"', INGREDIENTES='" +ingredientes.getText().toString() +"', PREPARACION='" +preparacion.getText().toString() +"', OBSERVACIONES='" +observaciones.getText().toString() +"' WHERE ID="+identificacion.getText().toString();

            tabla.execSQL(SQL);
            tabla.close();
            Toast.makeText(this,"Se actualizó",Toast.LENGTH_LONG).show();

        }catch (SQLiteException e){
            Toast.makeText(this,"ERROR: No se puede actualizar",Toast.LENGTH_LONG).show();
        }
        habilitarBotonesYLimpiarCampos();

    }

    private void habilitarBotonesYLimpiarCampos() {

        identificacion.setText("");
        nombre.setText("");
        ingredientes.setText("");
        preparacion.setText("");
        observaciones.setText("");
        insertar.setEnabled(true);
        consultar.setEnabled(true);
        eliminar.setEnabled(true);
        actualizar.setText("ACTUALIZAR");
        identificacion.setEnabled(true);

    }

    private void pedirID(final int origen) {

        final EditText pidoID = new EditText(this);
        String mensaje ="", mensajeButton = null;
        pidoID.setInputType(InputType.TYPE_CLASS_NUMBER);

        if(origen==1){
            mensaje = "ESCRIBE ID A BUSCAR";
            mensajeButton = "BUSCAR";
        }

        if(origen==2){
            mensaje = "ESCRIBE EL ID A ELIMINAR";
            mensajeButton = "Eliminr";
        }

        if(origen==3){
            mensaje= "ESCRIBE EL ID A MODIFICAR";
            mensajeButton = "Modificar";
        }

        pidoID.setHint(mensaje);

        AlertDialog.Builder alerta = new AlertDialog.Builder(this);

        alerta.setTitle("ATENCIÓN").setMessage(mensaje)
                .setView(pidoID)
                .setPositiveButton(mensajeButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(pidoID.getText().toString().isEmpty()){
                            Toast.makeText(MainActivity.this,"DEBES ESCRIBIR VALOR", Toast.LENGTH_LONG).show();
                            return;
                        }
                        buscarDato(pidoID.getText().toString(), origen);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("CANCELAR",null).show();
    }

    private void buscarDato(String idBuscar, int origen) {

        try{
            SQLiteDatabase tabla = base.getReadableDatabase();

            String SQL = "SELECT * FROM RECETA WHERE ID="+idBuscar;

            Cursor resultado = tabla.rawQuery(SQL, null);

            if(resultado.moveToFirst()){
                //Si hay resultado

                identificacion.setText(resultado.getString(0));
                nombre.setText(resultado.getString(1));
                ingredientes.setText(resultado.getString(2));
                preparacion.setText(resultado.getString(3));
                observaciones.setText(resultado.getString(4));

                if(origen==2){
                    //Esto siginifica que el resultó  para borrar

                    String dato = idBuscar+"&"+resultado.getString(1)+"&"+resultado.getString(2)+"&"+resultado.getString(3)+"&"+resultado.getString(4);
                    invocarConfirmacionEliminar(dato);
                    return;
                }

                if(origen==3){
                    //modificar
                    insertar.setEnabled(false);
                    consultar.setEnabled(false);
                    eliminar.setEnabled(false);
                    actualizar.setText("CONFIRMAR ACTUALIZACION");
                    identificacion.setEnabled(false);
                }
            }else {
                //No hay resultado
                Toast.makeText(this,"No se encontró resultado", Toast.LENGTH_LONG).show();
            }

            tabla.close();

        }catch (SQLiteException e){
            Toast.makeText(this,"ERROR: No se pudo", Toast.LENGTH_LONG).show();
        }
    }

    private void invocarConfirmacionEliminar(String dato) {

        String datos[] = dato.split("&");
        final String id = datos[0];
        String nombre = datos[1];

        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("IMPORTANTE").setMessage("¿Deseas eliminar al usuario: "+nombre+"?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        eliminarDato(id);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No",null).show();
    }

    private void eliminarDato(String idEliminar) {

        try {
            SQLiteDatabase tabla = base.getWritableDatabase();
            String SQL = "DELETE FROM RECETA WHERE ID="+idEliminar;
            tabla.execSQL(SQL);
            tabla.close();

            Toast.makeText(this,"ELIMINADO", Toast.LENGTH_LONG).show();

            vaciarCampos();

        }catch (SQLiteException e){
            Toast.makeText(this,"ERROR: No se pudo eliminar", Toast.LENGTH_LONG).show();
        }

    }

    private void codigoInsertar() {
        try{
            SQLiteDatabase tabla = base.getWritableDatabase();

            String SQL = "INSERT INTO RECETA VALUES("
                    +identificacion.getText().toString()
                    +",'"
                    +nombre.getText().toString()
                    +"','"
                    +ingredientes.getText().toString()
                    +"','"
                    +preparacion.getText().toString()
                    +"','"
                    +observaciones.getText().toString()
                    +"')";

            tabla.execSQL(SQL);
            Toast.makeText(this,"SI SE PUDO INSERTAR", Toast.LENGTH_LONG).show();

            tabla.close();

            vaciarCampos();
        }catch (SQLiteException e){
            Toast.makeText(this,"ERROR: No se pudo", Toast.LENGTH_LONG).show();
        }
    }

    private void vaciarCampos() {
        identificacion.setText(null);
        nombre.setText(null);
        ingredientes.setText(null);
        preparacion.setText(null);
        observaciones.setText(null);
    }
}
