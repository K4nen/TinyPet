package foo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.api.server.spi.auth.EspAuthenticator;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Projection;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.repackaged.com.google.datastore.v1.*;
import com.google.appengine.repackaged.com.google.datastore.v1.Value.Builder;
import com.google.appengine.repackaged.com.google.protobuf.StringValue;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.AdminDatastoreService.EntityBuilder;

@Api(name = "myApi", version = "v1", audiences = "629395974706-tfmev15r300qgu9mu3tntvle3lusu71r.apps.googleusercontent.com", clientIds = {
        "629395974706-tfmev15r300qgu9mu3tntvle3lusu71r.apps.googleusercontent.com" }, namespace = @ApiNamespace(ownerDomain = "helloworld.example.com", ownerName = "helloworld.example.com", packagePath = ""))

public class TPEndpoint {

    Random r = new Random();

    // remember: return Primitives and enums are not allowed.

    @ApiMethod(name = "getRandom", httpMethod = HttpMethod.GET)
    public RandomResult random(User user) throws UnauthorizedException {

        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return new RandomResult(r.nextInt(6) + 1);

    }

    // Récupérer l'entièreté des pétitions
    @ApiMethod(name = "gotAllPetitions", httpMethod = HttpMethod.GET)
    public CollectionResponse<Entity> gotAllPetitions(@Nullable @Named("next") String cursorString) {

        Query query = new Query("Petition").addSort("Date", SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery preparedquery = datastore.prepare(query);

        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
        if (cursorString != null) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
        }
        QueryResultList<Entity> results = preparedquery.asQueryResultList(fetchOptions);
        cursorString = results.getCursor().toWebSafeString();

        return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();

    }

    // Récupérer les pétitions créées par un utilisateur
    @ApiMethod(name = "gotMyPetitions", httpMethod = HttpMethod.GET)
    public CollectionResponse<Entity> gotMyPetitions(User user, @Nullable @Named("next") String cursorString)
            throws UnauthorizedException {

        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }
        Query query = new Query("Petition").setFilter(new FilterPredicate("Owner", FilterOperator.EQUAL, user.getId())).addSort("Date",SortDirection.DESCENDING);;
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery preparedquery = datastore.prepare(query);

        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(5);

        if (cursorString != null) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
        }

        QueryResultList<Entity> results = preparedquery.asQueryResultList(fetchOptions);
        cursorString = results.getCursor().toWebSafeString();

        return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();

    }

    // Récupérer les pétitions signées par un utilisateur
    @ApiMethod(name = "gotSignedPetitions", httpMethod = HttpMethod.GET)
    public CollectionResponse<Entity> gotSignedPetitions(User user, @Nullable @Named("next") String cursorString)
            throws UnauthorizedException {

        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }

        Query query = new Query("Petition").setFilter(new FilterPredicate("votants", FilterOperator.EQUAL, user.getId()))
                .addSort("Date",
                        SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery preparedquery = datastore.prepare(query);

        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(5);

        if (cursorString != null) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
        }

        QueryResultList<Entity> results = preparedquery.asQueryResultList(fetchOptions);
        cursorString = results.getCursor().toWebSafeString();

        return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();

    }

    // Récupérer une petition par son id
    @ApiMethod(name = "gotOnePetitionById", httpMethod = HttpMethod.GET)
    public Entity gotOnePetitionById(@Named("idpetition") String idPetition) {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query query = new Query("Petition");

        FilterPredicate filter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL,KeyFactory.createKey("Petition", idPetition));
        query.setFilter(filter);
        PreparedQuery preparedquery = datastore.prepare(query);
        Entity entity = preparedquery.asSingleEntity();

        return entity;

    }

   // Créer une pétition
    @ApiMethod(name = "addPetition", httpMethod = HttpMethod.GET)
    public Entity addPetition(User user, @Named("title") String title, @Named("description") String body)
        throws UnauthorizedException {

    // Vérifie si l'utilisateur est authentifié
    if (user == null) {
        throw new UnauthorizedException("Invalid credentials");
    }

    // Initialise la clé de la nouvelle pétition
    String finalKey = "P1";
    Key lastkey = getLastEntityKey();

    // Vérifie s'il existe déjà des pétitions
    if(lastkey != null){
        // Obtient le nombre de la dernière clé de pétition
        long lastkeyNum = Long.parseLong(lastkey.getName().substring(1));
        lastkeyNum++;
        String newKeyNum = Long.toString(lastkeyNum);

        // Crée une nouvelle clé pour la pétition
        Key newPetitionKey = KeyFactory.createKey(lastkey.getParent(), lastkey.getKind(), "P" + newKeyNum);

         finalKey = newPetitionKey.getName();
    }

    // Crée une nouvelle entité de type "Petition" avec la clé générée
    Entity entite = new Entity("Petition", finalKey);
    entite.setProperty("Date", new Date());
    entite.setProperty("Owner", user.getId());
    entite.setProperty("Body", body);
    entite.setProperty("Title", title);

    // Initialise une liste de votants avec l'utilisateur actuel
    HashSet<String> list = new HashSet<String>();
    list.add(user.getId());

    // Ajoute les propriétés "votants" (liste des votants) et "nbvotants" (nombre de votants) à l'entité
    entite.setProperty("votants", list);
    entite.setProperty("nbvotants", list.size());

    // Obtient le service Datastore et enregistre l'entité dans la base de données
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(entite);

    // Retourne l'entité de la pétition créée
    return entite;
}


    public Key getLastEntityKey() {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // Créez une requête pour récupérer la dernière entité créée
        Query query = new Query("Petition").addSort("Date", SortDirection.DESCENDING)
                .setKeysOnly();

        // Exécutez la requête et récupérez les résultats
        PreparedQuery preparedquery = datastore.prepare(query);
        Iterator<Entity> results = preparedquery.asIterable().iterator();

        // Récupérez la clé de la première (et unique) entité dans les résultats
        Entity entity = results.next();
        Key lastEntityKey = entity.getKey();

        return lastEntityKey;

    }

    // Signer une pétition
    @ApiMethod(name = "signPetition", httpMethod = HttpMethod.GET)
    public Entity signPetition(User user, @Named("petitionId") String id) throws UnauthorizedException {

    // Vérifie si l'utilisateur est authentifié
    if (user == null) {
        throw new UnauthorizedException("Invalid credentials");
    }

    // Obtient l'entité de la pétition spécifiée par l'ID
    Entity entite = gotOnePetitionById(id);

    // Obtient la liste des votants de l'entité
    ArrayList<String> list = (ArrayList<String>) entite.getProperty("votants");

    // Vérifie si l'utilisateur n'a pas encore signé la pétition
    if(!list.contains(user.getId())){
        // Ajoute l'ID de l'utilisateur à la liste des votants
        list.add(user.getId());
        entite.setProperty("votants", list);
        entite.setProperty("nbvotants", list.size());    
    }
    
    // Obtient le service Datastore et met à jour l'entité dans la base de données
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(entite);

    // Retourne l'entité de la pétition mise à jour
    return entite;
    }



}
