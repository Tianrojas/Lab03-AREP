package com.example.genericsLive;

/**
 * Interfaz funcional para representar una función que maneja una solicitud HTTP.
 */
public interface Function {
    /**
     * Método para manejar una solicitud HTTP y devolver una respuesta.
     *
     * @param requestQuery la cadena de consulta de la solicitud HTTP
     * @return la respuesta generada como una cadena
     */
    String handle(String requestQuery);
}
