# implementacion-sii
### ¿Que es esto?
En este repositorio dejaré (es la intención) el código fuente importante para la creación de las boletas, cesiones y facturas electronicas que define el SII (los famosos DTE), fue entretenido cuando desarrolle esos proyectos y ahora si pueden ser útiles para alguien mejor :blush:

Para mayor informacion: https://www.sii.cl

- El Primer proyecto es **bel.firma**: Permite realizar la firma de los documentos electronicos. Se usa de la siguiente manera:

-- sin uri para resolver dentro del archivo (¿firmar la semilla del SII?):
```
RequisitoFirma rf = new RequisitoFirma();
rf.setAlias("alias-jks");
rf.setClaveAlias("clave-alias");
rf.setClaveJks("clave-jks");
rf.setCodificacionEntrada("codificacion");
rf.setOmitirTagXml(true);
rf.setRevisarUriLocal(false);
rf.setRutaJks("ruta-al-punto-jks");
rf.setEntrada("contenido-para-firmar");
rf.setIdFirma(null);
Firmador firmador = new Firmador(rf);
firmador.firmar();
```
-- con uri para resolver dentro del archivo:
```
RequisitoFirma rf = new RequisitoFirma();
rf.setAlias("alias-jks");
rf.setClaveAlias("clave-alias");
rf.setClaveJks("clave-jks");
rf.setCodificacionEntrada("codificacion");
rf.setOmitirTagXml(true);
rf.setRevisarUriLocal(true);
rf.setRutaJks("ruta-al-punto-jks");
rf.setEntrada("contenido-para-firmar");
rf.setIdFirma("id-firma");
Firmador firmador = new Firmador(rf);
firmador.firmar();
```
- lo necesario para poder utilizarlo es el jks para la firma y el contenido del xml de entrada

***************

