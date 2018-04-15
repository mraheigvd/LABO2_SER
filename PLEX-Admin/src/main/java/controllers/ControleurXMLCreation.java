package controllers;

import ch.heigvd.iict.ser.imdb.models.Role;
import com.thoughtworks.xstream.XStream;
import javassist.compiler.ast.Keyword;
import models.*;
import org.jdom2.Attribute;
import org.jdom2.Element;
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
					globalData = ormAccess.GET_GLOBAL_DATA(); // récupération des données

                    // Create the main element
					Element racine = new Element("Main");

					// Get projections, films and actors
					// We will create a XML structure in a way that we will avoid
					// the duplication of data
					List<Projection> lstProjections = globalData.getProjections();
                    List<Film> lstFilms = new ArrayList<Film>();
                    List<Acteur> lstActeurs = new ArrayList<Acteur>();

                    for (Projection p : lstProjections) {
                        lstFilms.add(p.getFilm());
                        for (RoleActeur a : p.getFilm().getRoles()) {
                            lstActeurs.add(a.getActeur());
                        }
                    }

                    // Our main Elements after the Main element
                    Element projections = new Element("Projections");
                    Element acteurs = new Element("Acteurs");
                    Element films = new Element("Films");

                    for (Acteur a : lstActeurs) {
                        Element acteur = new Element("Acteur");
                        acteur.setAttribute("id", String.valueOf(a.getId()));
                        acteur.addContent(new Element("Nom").setText(a.getNom()));
                        acteur.addContent(new Element("Nom_Naissance").setText(a.getNomNaissance()));
                        acteur.addContent(new Element("Biographie").setText(a.getBiographie()));
                        acteur.addContent(new Element("Sexe").setText(a.getSexe().toString()));
                        String dateNaissance = a.getDateNaissance() == null ? "" : a.getDateNaissance().toString();
                        acteur.addContent(new Element("Date_Naissance").setText(dateNaissance));
                        String dateDeces = a.getDateDeces() == null ? "" : a.getDateDeces().toString();
                        acteur.addContent(new Element("Date_Deces").setText(dateDeces));
                        acteurs.addContent(acteur);
                    }

                    for (Film f : lstFilms) {
                        Element film = new Element("Film");
                        film.setAttribute("id", String.valueOf(f.getId()));
                        Element Film_Acteurs = new Element("Film_Acteurs");

                        Set<RoleActeur> lsRA = f.getRoles();
                        for (RoleActeur ra : lsRA) {
                            Element Film_Acteur = new Element("Film_Acteur");
                            Film_Acteur.setAttribute("id", String.valueOf(ra.getActeur().getId()));
                            Film_Acteur.addContent(new Element("Role_Nom").setText(ra.getPersonnage()));
                            Film_Acteur.addContent(new Element("Role_Place").setText(String.valueOf(ra.getPlace())));
                            film.addContent(Film_Acteur);
                        }

                        film.addContent(new Element("Titre").setText(f.getTitre()));;
                        film.addContent(new Element("Synopsis").setText(f.getSynopsis()));;
                        film.addContent(new Element("Duree").setText(String.valueOf(f.getDuree())));
                        Element Genres = new Element("Genres");
                        for (Genre g : f.getGenres()) {
                            Genres.addContent(new Element("Genre").setText(g.getLabel()));
                        }

                        // We can't set a null attribute
                        if (f.getPhoto() != null)
                        	film.addContent(new Element("Photo").setAttribute("url", f.getPhoto()));

                        Element Critiques = new Element("Critiques");
                        for (Critique c : f.getCritiques()) {
                            Element critique = new Element("Critique");
                            critique.addContent(new Element("Texte").setText(c.getTexte()));
                            critique.addContent(new Element("Note").setText(String.valueOf(c.getNote())));
                            Critiques.addContent(critique);
                        }
                        film.addContent(Critiques);

                        Element Motcles = new Element("Motcles");
                        for (Motcle m : f.getMotcles()) {
                            Motcles.addContent(new Element("Motcle").setText(m.getLabel()));
                        }
                        film.addContent(Motcles);

                        Element Langages = new Element("Langages");
                        for (Langage l : f.getLangages()) {
                            Motcles.addContent(new Element("Langage").setText(l.getLabel()));
                        }
                        film.addContent(Langages);
                        films.addContent(film);
                    }

                    // Create Projections
					for (Projection p : lstProjections) {
                        Element projection = new Element("Projection");
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
                        projection.addContent(new Element("Date").setText(simpleDateFormat.format(p.getDateHeure().getTime())));
                        projection.addContent(new Element("Salle").setText(p.getSalle().getNo()));
                        projection.addContent(new Element("Projection_Film").setAttribute(new Attribute("id", String.valueOf(p.getFilm().getId()))));
                        projections.addContent(projection);
                    }

                    racine.addContent(acteurs);
                    racine.addContent(films);
                    racine.addContent(projections);

					// Write xml file in a pretty format
					XMLOutputter xmlOutputter = new XMLOutputter();
					xmlOutputter.setFormat(Format.getPrettyFormat());

                    try {
                        xmlOutputter.output(racine, new FileWriter("SER_Labo2_PLEX_ADMIN.xml"));
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("Done in [" + displaySeconds(currentTime, System.currentTimeMillis()) + "]");
					mainGUI.setAcknoledgeMessage("Le fichier XML a été créé en "+ displaySeconds(currentTime, System.currentTimeMillis()) );

				} catch (Exception e){
					e.printStackTrace();
					mainGUI.setErrorMessage("Construction XML impossible", e.toString());
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



