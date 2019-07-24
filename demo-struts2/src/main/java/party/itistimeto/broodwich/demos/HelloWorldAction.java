package party.itistimeto.broodwich.demos;

import com.opensymphony.xwork2.ActionSupport;
//import org.eclipse.jetty.webapp.WebAppContext;
import party.itistimeto.broodwich.droppers.StrutsDropper;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class HelloWorldAction extends ActionSupport {
    private MessageStore messageStore;

    public String execute() {
        messageStore = new MessageStore() ;
        //StrutsDropper.taste();
        //WebAppContext.getCurrentWebAppContext().addFilter("party.itistimeto.broodwich.demos.DummyFilter", "/*", EnumSet.of(DispatcherType.REQUEST));

        return SUCCESS;
    }

    public MessageStore getMessageStore() {
        return messageStore;
    }
}
