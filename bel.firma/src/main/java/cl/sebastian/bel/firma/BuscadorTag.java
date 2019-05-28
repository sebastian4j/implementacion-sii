package cl.sebastian.bel.firma;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * clase para buscar un tag especifico dentro de un archivo y retornarlo sin modificarlo.
 * @author Sebastian Avila A.
 *
 */
public class BuscadorTag {
    /** archivo donde se busca el tag. */
    private final File entrada;
    /** contenido del archivo. */
    private final StringBuilder builder = new StringBuilder();
    /** codificacion del archivo. */
    private final Charset charset;
    /**
     * constructor.
     * @param entrada archivo donde buscar tags.
     * @param charset utilizado para la lectura del archivo
     * @throws IOException error de lectura
     */
    public BuscadorTag(final File entrada, final Charset charset) throws IOException {
        this.entrada = entrada;
        this.charset = charset;

        if (esEntradaCorrecta()) {
            cargarArchivo();
        } else {
            throw new IllegalArgumentException("el archivo indicado no es accesible");
        }
    }

    /**
     * determina si puede acceder al archivo para lectura.
     * @return
     */
    private boolean esEntradaCorrecta() {
        return entrada.exists() && entrada.canRead();
    }
    /**
     * metodo para buscar el tag indicado en el archivo de entrada.
     * @param tag
     * @return todas las coincidencias encontradas.
     */
    public String buscar(final String tag) {
        final Pattern patron = Pattern.compile(new StringBuilder().append("<(\\w*:){0,1}")
                .append(tag).append("([\\s*|>]|\\s\\w*)[\\w\\W]*<\\/(\\w*:){0,1}")
                .append(tag).append("([\\s*|>])").toString());

        final Matcher matcher = patron.matcher(builder.toString());
        String retorno = null;
        while (matcher.find()) {
            retorno = matcher.group();
        }
        return retorno;
    }

    /**
     * carga el archivo de entrada.
     * @throws IOException archivo de lectura no existe
     */
    private void cargarArchivo() throws IOException {
        try (final InputStreamReader isr = new InputStreamReader(new FileInputStream(entrada), charset);
                final BufferedReader br = new BufferedReader(isr)) {
            int lee;
            while ((lee = br.read()) != -1) {
                builder.append((char)lee);
            }
        }
    }
}
