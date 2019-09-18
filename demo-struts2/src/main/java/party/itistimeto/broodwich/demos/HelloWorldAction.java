/*
 * Copyright (c) 2019 Brian D. Hysell
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
