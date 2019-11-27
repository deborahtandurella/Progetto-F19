package dao;

import collection.Card;
import com.mysql.jdbc.Statement;
import platform.Platform;
import userSide.Exchange;
import userSide.User;

import java.sql.*;
import java.util.*;

import static java.sql.Connection.TRANSACTION_SERIALIZABLE;

public class ExchangeCardDAOImpl implements ExchangeCardDAO {
    MySQLDAOFactory connector = MySQLDAOFactory.getInstance();
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    ResultSet result = null;

    private static final String insert_transaction_query = "INSERT INTO exchanges (username) VALUES (?)";
    private static final String insert_cardown_query = "insert into cards_own(id_trans,cardId,quantity) values (?,?,?)";
    private static final String insert_cardWanted_query = "insert into cards_wanted(id_trans,cardId,quantity) values (?,?,?)";

    private static final String selectCollection = "select * from collections where ID_Card=? AND USERNAME=?";
    private static final String switchpeople = "insert into collections (ID_CARD,Username) values (?,?) "+
                                                    "ON DUPLICATE KEY UPDATE quantity=quantity+1";
    private static final String deleteCollection = "delete FROM collections WHERE Username=? and quantity=1 and ID_CARD=?";
    private static final String updateCollection = "update collections SET  quantity=quantity-1 WHERE  ID_CARD=? AND USERNAME=?";

    //transaction for update flag
    private static final String select_transaction = "select * from exchanges where id_trans=?";
    private static final String flag_complete = "update exchanges SET  USERNAME_Offer=? , trans_comp='1' WHERE id_trans=? ";


    private static final String get_exchange ="select exchanges.* , cards_own.cardId from (exchanges join cards_own ON cards_own.Id_trans=exchanges.Id_trans)  where exchanges.id_trans=?  ";
    private static final String get_cardWanted="select exchanges.* , cards_wanted.cardId from (exchanges join cards_wanted ON cards_wanted.Id_trans=exchanges.Id_trans)  where exchanges.id_trans=? ";
    private static final String get_all_exchange ="select exchanges.*,cards_wanted.cardId as want,cards_own.cardId as own,cards_own.quantity as oqt,cards_wanted.quantity as wqt from exchanges  join  cards_wanted on exchanges.Id_trans=cards_wanted.id_trans join cards_own on exchanges.Id_trans=cards_own.Id_trans  where exchanges.id_trans not in (select exchanges.Id_trans  from cards_wanted,collections,exchanges where cards_wanted.cardId not in (select collections.ID_Card from collections WHERE USERNAME=?)  and exchanges.id_trans=cards_wanted.Id_trans and exchanges.username!=? group by exchanges.id_trans)and username!=? and trans_comp='0'";

    private static final String delete_exchange = "SET AUTOcOMMIT=0" +
                                                    "start transaction;" +
                                                        "delete from exchanges where id_trans=?" +
                                                    "commit";
    //private static final String view_catalog = "select * from catalog";
    // query trova trattativa secondo nome della carta
    private static final String SEARCH_BY_NAME_CARD="SELECT exchanges.*, cards_own.cardId from (exchanges natural join cards_own) join catalog ON cards_own.cardId=catalog.ID WHERE catalog.CardName=?";
    private static final String SEARCH_BY_CATEGORY="SELECT exchanges.*, cards_own.cardId from (exchanges natural join cards_own) join catalog ON cards_own.cardId=catalog.ID WHERE catalog.Category=? ";
    private static final String SEARCH_BY_CLASS="SELECT exchanges.*, cards_own.cardId from (exchanges natural join cards_own) join catalog ON cards_own.cardId=catalog.ID WHERE catalog.Class=?";
    private static final String SEARCH_BY_TYPE="SELECT exchanges.*, cards_own.cardId from (exchanges natural join cards_own) join catalog ON cards_own.cardId=catalog.ID WHERE catalog.CardType=?";



    @Override
    public void create(User user, ArrayList<Card> cardown, ArrayList<Card> cardwanted) throws SQLException {
        conn = null;


        Map<Card, Integer> cardownRefactored = new HashMap<>();
        Map<Card, Integer> cardwantedRefactored = new HashMap<>();
        ArrayList<Integer> carteCensiteOwn = new ArrayList<>();
        ArrayList<Integer> carteCensiteWanted = new ArrayList<>();

        for (Card x : cardown) {
            if(!(carteCensiteOwn.contains(x.getId()))) {
                int quantità = 1;
                int posizioneAttuale = cardown.indexOf(x);
                for (int j = cardown.size() - 1; j > posizioneAttuale; j--) {
                    Card objDaParagonare = cardown.get(j);
                    if (x.getId() == objDaParagonare.getId()) {
                        quantità++;
                    }
                }
                cardownRefactored.put(x, quantità);
                carteCensiteOwn.add(x.getId());
            }
        }

        for (Card y : cardwanted) {
            if(!(carteCensiteWanted.contains(y.getId()))) {
                int quantità = 1;
                int posizioneAttuale = cardwanted.indexOf(y);
                for (int j = cardwanted.size() - 1; j > posizioneAttuale; j--) {
                    Card objDaParagonare = cardwanted.get(j);
                    if (y.getId() == objDaParagonare.getId()) {
                        quantità++;
                    }
                }
                cardwantedRefactored.put(y, quantità);
                carteCensiteWanted.add(y.getId());
            }
        }

        try {
            conn = connector.createConnection();

            preparedStatement = conn.prepareStatement(insert_transaction_query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.execute();

            result=preparedStatement.getGeneratedKeys();

            if (result.next() && result!=null)
            {
                for (Card c: cardownRefactored.keySet()) {
                    preparedStatement=conn.prepareStatement(insert_cardown_query);
                    preparedStatement.setInt(1,result.getInt(1));
                    preparedStatement.setInt(2,c.getId());
                    preparedStatement.setInt(3, cardownRefactored.get(c));
                    preparedStatement.execute();
                }
                for ( Card d: cardwantedRefactored.keySet()) {
                    preparedStatement=conn.prepareStatement(insert_cardWanted_query);
                    preparedStatement.setInt(1,result.getInt(1));
                    preparedStatement.setInt(2,d.getId());
                    preparedStatement.setInt(3, cardwantedRefactored.get(d));
                    preparedStatement.execute();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                result.close();
            } catch (Exception rse) {
                rse.printStackTrace();
            }
            try {
                preparedStatement.close();
            } catch (Exception sse) {
                sse.printStackTrace();
            }
            try {
                conn.close();
            } catch (Exception cse) {
                cse.printStackTrace();
            }
        }
    }

    @Override
    public boolean marketExchange(Exchange exchangeCard) {
        conn = null;
        Savepoint savepoint = null;
        Savepoint insert = null;
        Savepoint insert2 = null;
        //cancello carte presenti in entrambe le liste perchè è inutile scambaire due carte uguali
        try {
            //setto flag completato = 1 per evitare che altri utenti accettino lo stesso scambio
            conn = connector.createConnection();
            preparedStatement = conn.prepareStatement(select_transaction);
            //start transaction
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(TRANSACTION_SERIALIZABLE);
            preparedStatement.setInt(1,exchangeCard.getId_trans());
            preparedStatement.execute();
            savepoint=conn.setSavepoint();


            preparedStatement=conn.prepareStatement(flag_complete);
            preparedStatement.setString(1,exchangeCard.getUsername_offerente());
            preparedStatement.setInt(2,exchangeCard.getId_trans());
            preparedStatement.executeUpdate();


            //da all'utente le mie carte che vuole
            for(int i=0;i<exchangeCard.getId_card_wanted().size();i++)
            {
                //se è già nella collezione incremento la quantità
                //blocco le carte
                preparedStatement = conn.prepareStatement(selectCollection);
                preparedStatement.setInt(1,exchangeCard.getId_card_wanted().get(i));
                preparedStatement.setString(2,exchangeCard.getUsername_offerente());
                preparedStatement.execute();
                insert=conn.setSavepoint();

                //se è già nella collezione del tizio incrementa
                preparedStatement = conn.prepareStatement(switchpeople);
                preparedStatement.setInt(1,exchangeCard.getId_card_wanted().get(i));
                preparedStatement.setString(2,exchangeCard.getId_user());
                preparedStatement.execute();

                preparedStatement=conn.prepareStatement(deleteCollection);
                preparedStatement.setString(1,exchangeCard.getUsername_offerente());
                preparedStatement.setInt(2,exchangeCard.getId_card_wanted().get(i));
                preparedStatement.execute();
                conn.commit();

                preparedStatement=conn.prepareStatement(updateCollection);
                preparedStatement.setInt(1,exchangeCard.getId_card_wanted().get(i));
                preparedStatement.setString(2,exchangeCard.getUsername_offerente());
                preparedStatement.execute();
                conn.commit();
            }
            //da a me le carte dell'offerente
            for (int i=0;i<exchangeCard.get_id_card_owm().size();i++)
            {
                //se è già nella collezione incremento la quantità
                //blocco le carte
                preparedStatement = conn.prepareStatement(selectCollection);
                preparedStatement.setInt(1,exchangeCard.get_id_card_owm().get(i));
                preparedStatement.setString(2,exchangeCard.getId_user());
                preparedStatement.execute();
                insert2=conn.setSavepoint();


                //se è già nella collezione del tizio incrementa
                preparedStatement = conn.prepareStatement(switchpeople);
                preparedStatement.setInt(1,exchangeCard.get_id_card_owm().get(i));
                preparedStatement.setString(2,exchangeCard.getUsername_offerente());
                preparedStatement.execute();

                preparedStatement=conn.prepareStatement(deleteCollection);
                preparedStatement.setString(1,exchangeCard.getId_user());
                preparedStatement.setInt(2,exchangeCard.get_id_card_owm().get(i));
                preparedStatement.execute();
                conn.commit();

                preparedStatement=conn.prepareStatement(updateCollection);
                preparedStatement.setInt(1,exchangeCard.get_id_card_owm().get(i));
                preparedStatement.setString(2,exchangeCard.getId_user());
                preparedStatement.execute();
                conn.commit();
            }
            conn.setAutoCommit(true);
        }
        catch (SQLException e)
        {
            try {
                conn.rollback(savepoint);
                conn.rollback(insert);
                conn.rollback(insert2);
            }
            catch (SQLException e1)
            {

            }

            return true;
        }
        return false;


    }

    /**Retrun a exchange*/
    //inutile ho un ogegtto che è una lista di scambi... basta passarlo
    @Override
    public Exchange getExchange(int id_trans) throws SQLException {
        conn = null;
        try {
            conn = connector.createConnection();
            preparedStatement = conn.prepareStatement(get_exchange);
            preparedStatement.setInt(1, id_trans);
            preparedStatement.execute();
            result = preparedStatement.getResultSet();
            ArrayList<Integer> cardown =new ArrayList<>();
            ArrayList<Integer> cardwanted= new ArrayList<>();

            while (result.next()) {
                cardown.add(result.getInt("cardId"));
            }
            preparedStatement = conn.prepareStatement(get_cardWanted);
            preparedStatement.setInt(1, id_trans);
            preparedStatement.execute();
            result = preparedStatement.getResultSet();


            while (result.next()) {
                cardwanted.add(result.getInt("cardId"));
            }
            return new Exchange(id_trans, result.getString("username"), cardown,cardwanted, result.getBoolean("trans_comp"),result.getString("username_offer"));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                result.close();
            } catch (Exception rse) {
                rse.printStackTrace();
            }
            try {
                preparedStatement.close();
            } catch (Exception sse) {
                sse.printStackTrace();
            }
            try {
                conn.close();
            } catch (Exception cse) {
                cse.printStackTrace();
            }
            return null;
        }
    }

    /**Return all available exchanges */
    @Override
    public ArrayList<Exchange> getAllExchange(User user) throws SQLException {
        conn=null;
        Platform platform = Platform.getInstance();

        ArrayList<Integer> cardown = new ArrayList<>();
        ArrayList<Integer> cardwanted=new ArrayList<>();
        ArrayList<Exchange> allExchange = new ArrayList<>();
        try {
            conn = connector.createConnection();
            preparedStatement = conn.prepareStatement(get_all_exchange);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getUsername());
            preparedStatement.setString(3, user.getUsername());
            // preparedStatement.setString(2, user.getUsername());
            preparedStatement.execute();
            result = preparedStatement.getResultSet();
            int id_trans=0;
            String username_offer;
            if(result!=null && result.next()) {
                id_trans = result.getInt(1);
                cardown.add(result.getInt("own"));
                cardwanted.add(result.getInt("want"));
            }
            while (result != null) {
                cardown=new ArrayList<Integer>();
                cardwanted=new ArrayList<Integer>();
                username_offer=result.getString("username");
                while (result.getInt(1) == id_trans) {
                    if (!checkCard(cardown, result.getInt("own"))) {
                        cardown.add(result.getInt("own"));
                        addCard(result.getInt("oqt"), cardown, result.getInt("own"));
                    }
                    if (!checkCard(cardwanted, result.getInt("want"))) {
                        cardwanted.add(result.getInt("want"));
                        addCard(result.getInt("wqt"), cardown, result.getInt("want"));
                    }
                    if(result.next()==false)
                    {
                        result=null;
                        break;
                    }
                }
                //modificare null con nome offerente
                allExchange.add(new Exchange(id_trans,username_offer, cardown, cardwanted, false,null));
                if(result==null)
                {
                    break;
                }
                else
                {
                    id_trans = result.getInt("id_trans");
                }

            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                result.close();
            } catch (Exception rse) {
                rse.printStackTrace();
            }
            try {
                preparedStatement.close();
            } catch (Exception sse) {
                sse.printStackTrace();
            }
            try {
                conn.close();
            } catch (Exception cse) {
                cse.printStackTrace();
            }
        }
        return allExchange;
    }
    @Override
    public void delete(int id_trans) throws SQLException {
        conn = null;
        try {
            conn = connector.createConnection();
            preparedStatement = conn.prepareStatement(delete_exchange);
            preparedStatement.setInt(1, id_trans);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                result.close();
            } catch (Exception rse) {
                rse.printStackTrace();
            }
            try {
                preparedStatement.close();
            } catch (Exception sse) {
                sse.printStackTrace();
            }
            try {
                conn.close();
            } catch (Exception cse) {
                cse.printStackTrace();
            }
        }
    }

    /**..............METODI PER CERCARE LE TRATATTIVE**/
    /**Metodo che mi trova tratativa/e secondo il nome della carta*/
    public ArrayList<Exchange>findTByNameCard(User user, String nameCard) throws SQLException{
        conn=null;
        Exchange ex=null;
        ArrayList<Exchange> answer= new ArrayList<>();
        try{
            conn = connector.createConnection();
            preparedStatement = conn.prepareStatement(SEARCH_BY_NAME_CARD);
            preparedStatement.setString(1, nameCard);
            preparedStatement.execute();
            result = preparedStatement.getResultSet();
            while(result!=null && result.next()) {
                if(!result.getBoolean("trans_comp") && !result.getString("username").equals(user.getUsername())) {
                    result.previous();
                    while (result.next()) {
                        ex=getExchange(result.getInt("id_trans"));
                        answer.add(ex);
                    }
                }
            }

            return answer;

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                result.close();
            } catch (Exception rse) {
                rse.printStackTrace();
            }
            try {
                preparedStatement.close();
            } catch (Exception sse) {
                sse.printStackTrace();
            }
            try {
                conn.close();
            } catch (Exception cse) {
                cse.printStackTrace();
            }
        }
        return null;
    }

    /**Metodo che mi trova tratativa/e secondo la categoria*/
    public ArrayList<Exchange>findTByCategory(User user, String category) throws SQLException{
        conn=null;
        Exchange ex=null;
        ArrayList<Exchange> answer= new ArrayList<>();
        try{
            conn = connector.createConnection();
            preparedStatement = conn.prepareStatement(SEARCH_BY_CATEGORY);
            preparedStatement.setString(1, category);
            preparedStatement.execute();
            result = preparedStatement.getResultSet();
            while(result!=null && result.next()) {
                if(!result.getBoolean("trans_comp") && !result.getString("username").equals(user.getUsername())) {
                    result.previous();
                    while (result.next()) {
                        ex=getExchange(result.getInt("id_trans"));
                        answer.add(ex);
                    }
                }
            }

            return answer;

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                result.close();
            } catch (Exception rse) {
                rse.printStackTrace();
            }
            try {
                preparedStatement.close();
            } catch (Exception sse) {
                sse.printStackTrace();
            }
            try {
                conn.close();
            } catch (Exception cse) {
                cse.printStackTrace();
            }
        }
        return null;
    }

    /**Metodo che mi trova tratativa/e secondo la classe della carta*/
    public ArrayList<Exchange>findTByClassCard(User user, String classCard) throws SQLException{
        conn=null;
        Exchange ex=null;
        ArrayList<Exchange> answer= new ArrayList<>();
        try{
            conn = connector.createConnection();
            preparedStatement = conn.prepareStatement(SEARCH_BY_CLASS);
            preparedStatement.setString(1, classCard);
            preparedStatement.execute();
            result = preparedStatement.getResultSet();
            while(result!=null && result.next()) {
                if(!result.getBoolean("trans_comp") && !result.getString("username").equals(user.getUsername())) {
                    result.previous();
                    while (result.next()) {
                        ex=getExchange(result.getInt("id_trans"));
                        answer.add(ex);
                    }
                }
            }

            return answer;

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                result.close();
            } catch (Exception rse) {
                rse.printStackTrace();
            }
            try {
                preparedStatement.close();
            } catch (Exception sse) {
                sse.printStackTrace();
            }
            try {
                conn.close();
            } catch (Exception cse) {
                cse.printStackTrace();
            }
        }
        return null;
    }

    /**Metodo che mi trova tratativa/e secondo il tipo della carta*/
    public ArrayList<Exchange>findTByTypeCard(User user, String typeCard) throws SQLException{
        conn=null;
        Exchange ex=null;
        ArrayList<Exchange> answer= new ArrayList<>();
        try{
            conn = connector.createConnection();
            preparedStatement = conn.prepareStatement(SEARCH_BY_TYPE);
            preparedStatement.setString(1, typeCard);
            preparedStatement.execute();
            result = preparedStatement.getResultSet();
            while(result!=null && result.next()) {
                if(!result.getBoolean("trans_comp") && !result.getString("username").equals(user.getUsername())) {
                    result.previous();
                    while (result.next()) {
                        ex=getExchange(result.getInt("id_trans"));
                        answer.add(ex);
                    }
                }
            }

            return answer;

        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                result.close();
            } catch (Exception rse) {
                rse.printStackTrace();
            }
            try {
                preparedStatement.close();
            } catch (Exception sse) {
                sse.printStackTrace();
            }
            try {
                conn.close();
            } catch (Exception cse) {
                cse.printStackTrace();
            }
        }
        return null;
    }
    public boolean checkCard(ArrayList<Integer> cards, int toSearch)
    {
        //metodo che controlla se elemento è già presente nell'array
            for (int i=0; i<cards.size(); i++)
                if (cards.contains(toSearch))
                    return true;
            return false;
        }
        public void addCard(int quantity, ArrayList<Integer> cardsArray, int card)
        {
            if(quantity>1)
            {
               while (quantity!=1)
                {
                    cardsArray.add(card);
                    quantity--;
                }
            }
        }
/**..................FINE METODI PER LA RICERCA DI TRATTATIVE**/


}
