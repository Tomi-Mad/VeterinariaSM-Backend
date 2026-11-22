package cl.sanmarcos.usuarios.util;

/** Normaliza y valida RUT chileno (dígito verificador, módulo 11). */
public final class RutUtil {

    private RutUtil() {}

    /** "12.345.678-5" / "123456785" -> "12345678-5". Devuelve null si el texto es vacío. */
    public static String normalizar(String rut) {
        if (rut == null) return null;
        String r = rut.replace(".", "").replace(" ", "").replace("-", "").toUpperCase();
        if (r.length() < 2) return r.isEmpty() ? null : r;
        return r.substring(0, r.length() - 1) + "-" + r.charAt(r.length() - 1);
    }

    /** Recibe un RUT ya normalizado. */
    public static boolean esValido(String rutNormalizado) {
        if (rutNormalizado == null || !rutNormalizado.matches("\\d{7,8}-[0-9K]")) return false;
        String cuerpo = rutNormalizado.substring(0, rutNormalizado.length() - 2);
        char dv = rutNormalizado.charAt(rutNormalizado.length() - 1);
        int suma = 0, factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int resto = 11 - (suma % 11);
        char esperado = resto == 11 ? '0' : resto == 10 ? 'K' : (char) ('0' + resto);
        return dv == esperado;
    }
}
