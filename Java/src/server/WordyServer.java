package server;

import constants.Constants;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import wordyGame.WordyInt;
import wordyGame.WordyIntHelper;

import java.sql.SQLException;
public class WordyServer {
    public static void main(String[] args) throws SQLException {
        try {

            ORB orb = ORB.init(args, null);


            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references(Constants.ROOT_POA_OBJECT_NAME));
            rootpoa.the_POAManager().activate();

            WordyImpl impl = new WordyImpl();
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(impl);

            WordyInt href = WordyIntHelper.narrow(ref);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references(Constants.NAME_SERVICE_OBJECT_NAME);
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            String name = "WordyApp";

            NameComponent[] path = ncRef.to_name(name);
            ncRef.rebind(path, href);
            System.out.println("Server up.");
            orb.run();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("WordyServer Exiting");
    }



}
