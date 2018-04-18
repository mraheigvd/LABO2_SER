package controllers;

import com.thoughtworks.xstream.XStream;
import models.*;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import views.MainGUI;

import java.io.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.*;





/**
 * @Author: Mentor Reka & Kamil Amrani
 * @Brief: Write data into XML conforming to Labo1 definition
 * @Date: 15.04.2018
 */
public class ControleurXMLCreation {

	//private ControleurGeneral ctrGeneral;
	private static MainGUI mainGUI;
	private ORMAccess ormAccess;

	private GlobalData globalData;
	private static final String DATE_FORMAT = "dd-MM-yyyy - hh:mm";
	private static final String XML_FILE = "SER_Labo2_PLEX_ADMIN.xml";

	/**
	 * A inner class in order to validate the XML document with the specified DTD
	 */
	public static class ValidateXmlDtdJdom {

		public static void validate() throws JDOMException, IOException {

			// read file from classpath
			File f = new File( XML_FILE);
			InputStream valid = ControleurXMLCreation.class.getResourceAsStream(XML_FILE);

			// create builder
			SAXBuilder builder = new SAXBuilder(XMLReaders.DTDVALIDATING);
			Document validDocument = builder.build(f);

			// print metadata doc type
			DocType docType = validDocument.getDocType();
			System.out.println("Public ID: " + docType.getPublicID());
			System.out.println("System ID: " + docType.getSystemID());
			System.out.println("The document " + XML_FILE + " was validated with his DTD :" + docType.getSystemID());

		}
	}


	public ControleurXMLCreation(ControleurGeneral ctrGeneral, MainGUI mainGUI, ORMAccess ormAccess){
		//this.ctrGeneral=ctrGeneral;
		ControleurXMLCreation.mainGUI=mainGUI;
		this.ormAccess=ormAccess;
	}


	public void createXML(){
		new Thread(){
			public void run(){
				mainGUI.setAcknoledgeMessage("Creation XML... WAIT");
				long currentTime = System.currentTimeMillis();
				try {
					globalData = ormAccess.GET_GLOBAL_DATA(); // Get datas

                    // Create the main element
					Document doc = new Document();

					// Create the DocType and reference the external DTD
					DocType docTypeWithDTD = new DocType("Main",
							"SYSTEM",
							"SER_Labo1_PLEX_ADMIN.dtd");
					doc.setDocType(docTypeWithDTD);
					Element root = new Element("Main");

					// Get projections, films and actors
					// We will create a XML structure in a way that we will avoid
					// the duplication of data
					List<Projection> lstProjections = globalData.getProjections();
                    List<Film> lstFilms = new ArrayList<Film>();
                    List<Acteur> lstActeurs = new ArrayList<Acteur>();

                    for (Projection p : lstProjections) {
                    	if (!lstFilms.contains(p.getFilm()))
                        	lstFilms.add(p.getFilm());
                        for (RoleActeur a : p.getFilm().getRoles()) {
                        	if (!lstActeurs.contains(a.getActeur()))
                            	lstActeurs.add(a.getActeur());
                        }
                    }

                    // Our main Elements after the Main element
                    Element projections = new Element("Projections");
                    Element acteurs = new Element("Acteurs");
                    Element films = new Element("Films");
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);

					// Add actors
                    for (Acteur a : lstActeurs) {
                        Element acteur = new Element("Acteur");
                        acteur.setAttribute("id", "id_" + String.valueOf(a.getId()));
                        acteur.addContent(new Element("Nom").setText(a.getNom()));
                        acteur.addContent(new Element("Nom_Naissance").setText(a.getNomNaissance()));
                        acteur.addContent(new Element("Biographie").setText(a.getBiographie()));
                        acteur.addContent(new Element("Sexe").setText(a.getSexe().toString()));

						String dateNaissance = a.getDateNaissance() == null ? "" : simpleDateFormat.format(a.getDateNaissance().getTime());
                        acteur.addContent(new Element("Date_Naissance").setText(dateNaissance));
						String dateDeces = a.getDateDeces() == null ? "" : simpleDateFormat.format(a.getDateDeces().getTime());
                        acteur.addContent(new Element("Date_Deces").setText(dateDeces));
                        acteurs.addContent(acteur);
                    }

                    // Add films
                    for (Film f : lstFilms) {
                        Element film = new Element("Film");
                        film.setAttribute("id", "id_" + String.valueOf(f.getId()));
                        Element Film_Acteurs = new Element("Film_Acteurs");

                        Set<RoleActeur> lsRA = f.getRoles();
                        for (RoleActeur ra : lsRA) {
                            Element Film_Acteur = new Element("Film_Acteur");
                            Film_Acteur.setAttribute("id", "id_" + String.valueOf(ra.getActeur().getId()));
                            Film_Acteur.addContent(new Element("Role_Nom").setText(ra.getPersonnage()));
                            Film_Acteur.addContent(new Element("Role_Place").setText(String.valueOf(ra.getPlace())));
							Film_Acteurs.addContent(Film_Acteur);
                        }
                        film.addContent(Film_Acteurs);

                        film.addContent(new Element("Titre").setText(f.getTitre()));;
                        film.addContent(new Element("Synopsis").setText(f.getSynopsis()));;
                        film.addContent(new Element("Duree").setText(String.valueOf(f.getDuree())));
                        Element Genres = new Element("Genres");
                        for (Genre g : f.getGenres()) {
                            Genres.addContent(new Element("Genre").setText(g.getLabel()));
                        }
                        film.addContent(Genres);

                        // We can't set a null attribute
						Element Photo = new Element("Photo");
                        if (f.getPhoto() != null)
                        	Photo.setAttribute("url", f.getPhoto());
                        else
                        	Photo.setAttribute("url", "null");
                        film.addContent(Photo);

                        Element Critiques = new Element("Critiques");
                        for (Critique c : f.getCritiques()) {
                            Element critique = new Element("Critique");
                            critique.addContent(new Element("Texte").setText(c.getTexte()));
                            critique.addContent(new Element("Note").setText(String.valueOf(c.getNote())));
                            Critiques.addContent(critique);
                        }
                        film.addContent(Critiques);

                        Element Keywords = new Element("Keywords");
                        for (Motcle m : f.getMotcles()) {
							Keywords.addContent(new Element("Keyword").setText(m.getLabel()));
                        }
                        film.addContent(Keywords);

                        Element Langages = new Element("Langages");
                        for (Langage l : f.getLangages()) {
							Langages.addContent(new Element("Langage").setText(l.getLabel()));
                        }
                        film.addContent(Langages);
                        films.addContent(film);
                    }

                    // Create Projections
					for (Projection p : lstProjections) {
                        Element projection = new Element("Projection");
                        projection.addContent(new Element("Date").setText(simpleDateFormat.format(p.getDateHeure().getTime())));
                        projection.addContent(new Element("Salle").setText(p.getSalle().getNo()));
                        projection.addContent(new Element("Projection_Film").setAttribute(new Attribute("id", "id_" + String.valueOf(p.getFilm().getId()))));
                        projections.addContent(projection);
                    }

                    // finaly add submain elements to the root
                    root.addContent(acteurs);
                    root.addContent(films);
                    root.addContent(projections);
                    doc.addContent(root);

					// Write xml file in a pretty format
					XMLOutputter xmlOutputter = new XMLOutputter();
					xmlOutputter.setFormat(Format.getPrettyFormat());

                    try {
                    	// Write the result
                        xmlOutputter.output(doc, new FileWriter(XML_FILE));
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("Done in [" + displaySeconds(currentTime, System.currentTimeMillis()) + "]");
					mainGUI.setAcknoledgeMessage("Le fichier XML a été créé en "+ displaySeconds(currentTime, System.currentTimeMillis()) );
				} catch (Exception e){
					e.printStackTrace();
					mainGUI.setErrorMessage("Construction XML impossible", e.toString());
				} finally {
					// Finaly we will check that the XML generated respect the DTD
					try {
						ValidateXmlDtdJdom.validate();
					} catch (JDOMException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	public void createXStreamXML(){
		new Thread(){
				public void run(){
					mainGUI.setAcknoledgeMessage("Creation XML... WAIT");
					long currentTime = System.currentTimeMillis();
					try {
						globalData = ormAccess.GET_GLOBAL_DATA();
						globalDataControle();
					}
					catch (Exception e){
						mainGUI.setErrorMessage("Construction XML impossible", e.toString());
					}

					XStream xstream = new XStream();
					writeToFile("global_data.xml", xstream, globalData);
					System.out.println("Done [" + displaySeconds(currentTime, System.currentTimeMillis()) + "]");
					mainGUI.setAcknoledgeMessage("XML cree en "+ displaySeconds(currentTime, System.currentTimeMillis()) );
				}
		}.start();
	}

	private static void writeToFile(String filename, XStream serializer, Object data) {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
			serializer.toXML(data, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final DecimalFormat doubleFormat = new DecimalFormat("#.#");
	private static final String displaySeconds(long start, long end) {
		long diff = Math.abs(end - start);
		double seconds = ((double) diff) / 1000.0;
		return doubleFormat.format(seconds) + " s";
	}

	private void globalDataControle(){
		for (Projection p:globalData.getProjections()){
			System.out.println("******************************************");
			System.out.println(p.getFilm().getTitre());
			System.out.println(p.getSalle().getNo());
			System.out.println("Acteurs *********");
			for(RoleActeur role : p.getFilm().getRoles()) {
				System.out.println(role.getActeur().getNom());
			}
			System.out.println("Genres *********");
			for(Genre genre : p.getFilm().getGenres()) {
				System.out.println(genre.getLabel());
			}
			System.out.println("Mot-cles *********");
			for(Motcle motcle : p.getFilm().getMotcles()) {
				System.out.println(motcle.getLabel());
			}
			System.out.println("Langages *********");
			for(Langage langage : p.getFilm().getLangages()) {
				System.out.println(langage.getLabel());
			}
			System.out.println("Critiques *********");
			for(Critique critique : p.getFilm().getCritiques()) {
				System.out.println(critique.getNote());
				System.out.println(critique.getTexte());
			}
		}
	}
}



