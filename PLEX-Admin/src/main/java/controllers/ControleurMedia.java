package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.*;
import views.*;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @Author: Mentor Reka & Kamil Amrani
 * @Brief: Write data into JSON conforming to Labo1 definition
 * @Date: 15.04.2018
 */
public class ControleurMedia {

	private ControleurGeneral ctrGeneral;
	private static MainGUI mainGUI;
	private ORMAccess ormAccess;
	private GlobalData globalData;
	private static final String DATE_FORMAT = "dd-MM-yyyy - hh:mm";


	public ControleurMedia(ControleurGeneral ctrGeneral, MainGUI mainGUI, ORMAccess ormAccess){
		this.ctrGeneral=ctrGeneral;
		ControleurMedia.mainGUI=mainGUI;
		this.ormAccess=ormAccess;
	}


	/**
	 * List of projections only used for JSON parsing
	 */
	protected class ProjectionsJSON {
		private List<ProjectionNode> projections;

		/**
		 * Create a inner class for creating a JSON representation of Projection
		 */
		 protected class ProjectionNode {

			private String date;
			private String titre;
			private String premierRole;
			private String secondRole;

			protected ProjectionNode(String date, String titre, String premierRole, String secondRole) {
				this.date = date;
				this.titre = titre;
				this.premierRole = premierRole;
				this.secondRole = secondRole;
			}
		}

		protected ProjectionsJSON() {	}

		protected ProjectionsJSON(List<ProjectionNode> projections) {
			this.projections = projections;
		}

		public void setProjections(List<ProjectionNode> projections) {
			this.projections = projections;
		}
	}

	public void sendJSONToMedia(){


		new Thread(){
			public void run(){
				mainGUI.setAcknoledgeMessage("Envoi JSON ... WAIT");
				long currentTime = System.currentTimeMillis();
				try {
					globalData = ormAccess.GET_GLOBAL_DATA();


					ProjectionsJSON  projections = new ProjectionsJSON();
					List<ProjectionsJSON.ProjectionNode> lstProjection = new ArrayList<ProjectionsJSON.ProjectionNode>();

					// Populate the projections list with datas
					for (Projection p : globalData.getProjections()) {
						Set<RoleActeur> rolesActeurs = p.getFilm().getRoles();
						Iterator it = rolesActeurs.iterator();
						Acteur premierRole = ((RoleActeur) it.next()).getActeur();
						Acteur deuxiemeRole = ((RoleActeur) it.next()).getActeur();
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
						ProjectionsJSON.ProjectionNode proj = new ProjectionsJSON().new ProjectionNode(simpleDateFormat.format(p.getDateHeure().getTime()), p.getFilm().getTitre(),
								premierRole.getNom(), deuxiemeRole.getNom());
						lstProjection.add(proj);
					}
					projections.setProjections(lstProjection);

					// Write the list of ProjectionNode into a pretty format
					try (PrintWriter writer = new PrintWriter(new FileWriter("SER_Labo2_PLEX_ADMIN.json"))) {
						Gson gson = new GsonBuilder().setPrettyPrinting().create();
						gson.toJson(projections, writer);
					}
					System.out.println("Done in [" + displaySeconds(currentTime, System.currentTimeMillis()) + "]");
					mainGUI.setAcknoledgeMessage("Le fichier XML a été créé en "+ displaySeconds(currentTime, System.currentTimeMillis()) );

				}
				catch (Exception e){
					mainGUI.setErrorMessage("Construction XML impossible", e.toString());
				}
				System.out.println("Done in [" + displaySeconds(currentTime, System.currentTimeMillis()) + "]");

			}
		}.start();
	}

	private static final DecimalFormat doubleFormat = new DecimalFormat("#.#");

	private static final String displaySeconds(long start, long end) {
		long diff = Math.abs(end - start);
		double seconds = ((double) diff) / 1000.0;
		return doubleFormat.format(seconds) + " s";
	}

}