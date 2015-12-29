package praxisprojekt.dboo;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import praxisprojekt.dboo.backend.Movie;
import praxisprojekt.dboo.backend.MovieService;

import javax.servlet.annotation.WebServlet;

/* User Interface written in Java.
 *
 * Define the user interface shown on the Vaadin generated web page by extending the UI class.
 * By default, a new UI instance is automatically created when the page is loaded. To reuse
 * the same instance, add @PreserveOnRefresh.
 */
@Title("Praxisprojekt")
@Theme("valo")
public class MovieUI extends UI {

	/* Hundreds of widgets.
	 * Vaadin's user interface components are just Java objects that encapsulate
	 * and handle cross-browser support and client-server communication. The
	 * default Vaadin components are in the com.vaadin.ui package and there
	 * are over 500 more in vaadin.com/directory.
     */
    TextField filter = new TextField();
    Grid movieList = new Grid();
    Button newMovie = new Button("Neuer Eintrag");

    // ContactForm is an example of a custom component class
    MovieForm movieForm = new MovieForm();

    // ContactService is a in-memory mock DAO that mimics
    // a real-world datasource. Typically implemented for
    // example as EJB or Spring Data based service.
    MovieService service = MovieService.createDemoService();


    /* The "Main method".
     *
     * This is the entry point method executed to initialize and configure
     * the visible user interface. Executed on every browser reload because
     * a new instance is created for each web page loaded.
     */
    @Override
    protected void init(VaadinRequest request) {
        configureComponents();
        buildLayout();
    }


    private void configureComponents() {
         /* Synchronous event handling.
         *
         * Receive user interaction events on the server-side. This allows you
         * to synchronously handle those events. Vaadin automatically sends
         * only the needed changes to the web page without loading a new page.
         */
        newMovie.addClickListener(e -> movieForm.edit(new Movie()));

        filter.setInputPrompt("Filme filtern...");
        filter.addTextChangeListener(e -> refreshMovies(e.getText()));

        movieList.setContainerDataSource(new BeanItemContainer<>(Movie.class));
        movieList.setColumnOrder("filmname", "jahr");
        movieList.removeColumn("id");
        movieList.removeColumn("regisseur");
        movieList.removeColumn("birthDate");
        movieList.removeColumn("schauspieler");
        movieList.setSelectionMode(Grid.SelectionMode.SINGLE);
        movieList.addSelectionListener(e
                -> movieForm.edit((Movie) movieList.getSelectedRow()));
        refreshMovies();
    }

    /* Robust layouts.
     *
     * Layouts are components that contain other components.
     * HorizontalLayout contains TextField and Button. It is wrapped
     * with a Grid into VerticalLayout for the left side of the screen.
     * Allow user to resize the components with a SplitPanel.
     *
     * In addition to programmatically building layout in Java,
     * you may also choose to setup layout declaratively
     * with Vaadin Designer, CSS and HTML.
     */
    private void buildLayout() {
        HorizontalLayout actions = new HorizontalLayout(filter, newMovie);
        actions.setWidth("100%");
        filter.setWidth("100%");
        actions.setExpandRatio(filter, 1);

        VerticalLayout left = new VerticalLayout(actions, movieList);
        left.setSizeFull();
        movieList.setSizeFull();
        left.setExpandRatio(movieList, 1);

        HorizontalLayout mainLayout = new HorizontalLayout(left, movieForm);
        mainLayout.setSizeFull();
        mainLayout.setExpandRatio(left, 1);

        // Split and allow resizing
        setContent(mainLayout);
    }

    /* Choose the design patterns you like.
     *
     * It is good practice to have separate data access methods that
     * handle the back-end access and/or the user interface updates.
     * You can further split your code into classes to easier maintenance.
     * With Vaadin you can follow MVC, MVP or any other design pattern
     * you choose.
     */
    void refreshMovies() {
        refreshMovies(filter.getValue());
    }

    private void refreshMovies(String stringFilter) {
        movieList.setContainerDataSource(new BeanItemContainer<>(
                Movie.class, service.findAll(stringFilter)));
        movieForm.setVisible(false);
    }




    /*  Deployed as a Servlet or Portlet.
     *
     *  You can specify additional servlet parameters like the URI and UI
     *  class name and turn on production mode when you have finished developing the application.
     */
    @WebServlet(urlPatterns = "/*")
    @VaadinServletConfiguration(ui = MovieUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }


}
