package com.example.genericsLive;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Clase para manejar las solicitudes HTTP y generar respuestas basadas en el método y la URI recibidos.
 */
public class HttpRequestHandler {

    /**
     * Maneja la solicitud HTTP y genera una respuesta basada en el método y la URI recibidos.
     *
     * @param method el método HTTP de la solicitud (GET, POST, etc.)
     * @param uri    la URI de la solicitud
     * @return un objeto JSON que representa la respuesta generada
     */
    public static JSONObject handleRequest(String method, URI uri) {
        switch (method) {
            case "GET":
                try {
                    return handleGetRequest(uri);
                } catch (IOException e) {
                    return new JSONObject(Map.of("Title", "Resource not found", "Year", "Sorry", "Poster", "https://img.freepik.com/vector-premium/lindo-gato-triste-sentado-lluvia-nube-dibujos-animados-vector-icono-ilustracion-animal-naturaleza-icono-aislado_138676-5215.jpg?w=826"));
                }
            case "POST":
                return handlePostRequest(uri);
            // Agregar casos para otros verbos REST como PUT, DELETE, etc. según sea necesario
            default:
                return new JSONObject(Map.of("Title", "HTTP/1.1 405 Method Not Allowed"));
        }
    }

    /**
     * Maneja las solicitudes GET y genera una respuesta basada en la URI recibida.
     *
     * @param uri la URI de la solicitud GET
     * @return un objeto JSON que representa la respuesta generada
     * @throws IOException si ocurre un error durante el procesamiento de la solicitud
     */
    private static JSONObject handleGetRequest(URI uri) throws IOException {
        String movieName = uri.getQuery().split("=")[1];
        String normalizedMovieName = normalizeMovieName(movieName);

        JSONObject movieJSN = Cache.get(normalizedMovieName);
        if (movieJSN != null) {
            System.out.println("------------------------------------cache used");
        } else {
            System.out.println("------------------------------------resource used");
            HttpConnection.setMovieName(movieName);
            movieJSN = HttpConnection.getMovieJSN();
            Cache.put(normalizedMovieName, movieJSN);
        }
        return movieJSN;
    }

    /**
     * Normaliza el nombre de la película para su uso en la caché.
     *
     * @param movieName el nombre de la película a normalizar
     * @return el nombre de la película normalizado
     */
    private static String normalizeMovieName(String movieName) {
        return movieName.toLowerCase().trim();
    }

    /**
     * Maneja las solicitudes POST y genera una respuesta basada en la URI recibida.
     * Ejemplo de petición:
     * curl -X POST http://localhost:35000/movie -d "Title=Guardians%20of%20the%20Galaxy&Year=9999&Rated=PG-13&Released=01%20Aug%202014&Runtime=121%20min&Genre=Action,%20Adventure,%20Comedy&Director=James%20Gunn&Writer=James%20Gunn,%20Nicole%20Perlman,%20Dan%20Abnett&Actors=Chris%20Pratt,%20Vin%20Diesel,%20Bradley%20Cooper&Plot=A%20group%20of%20intergalactic%20criminals%20must%20pull%20together%20to%20stop%20a%20fanatical%20warrior%20with%20plans%20to%20purge%20the%20universe.&Language=English&Country=United%20States&Awards=Nominated%20for%202%20Oscars.%2052%20wins%20&%20103%20nominations%20total&Poster=https://www.scribbler.com/Images/Product/Default/medium/SCR2147.jpg&Ratings=[{"Source":"Internet%20Movie%20Database","Value":"8.0/10"},{"Source":"Rotten%20Tomatoes","Value":"92%"},{"Source":"Metacritic","Value":"76/100"}]&Metascore=76&imdbRating=8.0&imdbVotes=1,261,888&imdbID=tt2015381&Type=movie&DVD=15%20Nov%202015&BoxOffice=$333,718,600&Production=N/A&Website=N/A&Response=True"
     *
     * @param uri la URI de la solicitud POST
     * @return un objeto JSON que representa la respuesta generada
     */
    private static JSONObject handlePostRequest(URI uri) {
        String queryString = uri.getQuery();
        try {
            String encodedQueryString = queryString.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            String decodedQueryString = URLDecoder.decode(encodedQueryString, StandardCharsets.UTF_8.toString());
            String[] params = decodedQueryString.split("&");
            JSONObject newData = new JSONObject();
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    newData.put(key, value);
                }
            }
            String normalizedMovieName = normalizeMovieName(newData.optString("Title", ""));
            Cache.put(normalizedMovieName, newData);

            return Cache.get(normalizedMovieName);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error al decodificar la cadena de consulta: " + e.getMessage(), e);
        }
    }
}
