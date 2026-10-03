package es.jfgb.facturacion.verifactu.aeat;

public record ResultadoConsultaRegistroAeat(

        EstadoConsultaRegistroAeat estado,

        String estadoRegistroAeat,

        String codigoErrorRegistro,

        String descripcionErrorRegistro,

        int numeroCoincidencias) {

    public boolean esConcluyente() {

        return estado
                == EstadoConsultaRegistroAeat.ENCONTRADO
                || estado
                == EstadoConsultaRegistroAeat.NO_ENCONTRADO;
    }

    public static ResultadoConsultaRegistroAeat encontrado(
            String estadoRegistroAeat,
            String codigoErrorRegistro,
            String descripcionErrorRegistro) {

        return new ResultadoConsultaRegistroAeat(
                EstadoConsultaRegistroAeat.ENCONTRADO,
                estadoRegistroAeat,
                codigoErrorRegistro,
                descripcionErrorRegistro,
                1
        );
    }

    public static ResultadoConsultaRegistroAeat noEncontrado() {

        return new ResultadoConsultaRegistroAeat(
                EstadoConsultaRegistroAeat.NO_ENCONTRADO,
                null,
                null,
                null,
                0
        );
    }

    public static ResultadoConsultaRegistroAeat ambiguo(
            int numeroCoincidencias) {

        return new ResultadoConsultaRegistroAeat(
                EstadoConsultaRegistroAeat.AMBIGUO,
                null,
                null,
                null,
                numeroCoincidencias
        );
    }

    public static ResultadoConsultaRegistroAeat noConcluyente(
            int numeroCoincidencias) {

        return new ResultadoConsultaRegistroAeat(
                EstadoConsultaRegistroAeat.RESPUESTA_NO_CONCLUYENTE,
                null,
                null,
                null,
                numeroCoincidencias
        );
    }
}