package org.wotopul;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO find suitable Kotlin XML-library and rewrite it entirely in Kotlin

public class Automaton {
    public static class Event {
        public final String name;
        public final String comment;

        public Event(String name, String comment) {
            this.name = name;
            this.comment = comment;
        }
    }

    public static class State {
        public final int id;
        public final String name;
        public final int type;

        public final List<Integer> incoming;
        public final List<Integer> outgoing;

        public State(int id, String name, int type, List<Integer> incoming, List<Integer> outgoing) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.incoming = incoming;
            this.outgoing = outgoing;
        }
    }

    public static class Transition {
        public final int id;
        public final String event;
        public final String guard;
        public final List<String> actions;
        public final String code;

        public Transition(int id, String event, String guard, List<String> actions, String code) {
            this.id = id;
            this.event = event;
            this.guard = guard;
            this.actions = actions;
            this.code = code;
        }
    }

    public final List<Event> events;
    public final List<State> states;
    public final List<Transition> transitions;

    public Automaton(List<Event> events, List<State> states, List<Transition> transitions) {
        this.events = events;
        this.states = states;
        this.transitions = transitions;
    }

    public static Automaton readAutomatonFromFile(String filename)
        throws ParserConfigurationException, IOException, SAXException
    {
        File file = new File(filename);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        List<Event> events = new ArrayList<>();
        List<State> states = new ArrayList<>();
        List<Transition> transitions = new ArrayList<>();

        Element stateMachine = (Element) doc.getElementsByTagName("Statemachine").item(0);
        NodeList eventsList = stateMachine.getElementsByTagName("event");
        for (int i = 0; i < eventsList.getLength(); i++) {
            Element e = (Element) eventsList.item(i);
            String name = e.getAttribute("name");
            String comment = e.getAttribute("comment");
            events.add(new Event(name, comment));
        }

        NodeList widgets = doc.getElementsByTagName("widget");
        for (int i = 0; i < widgets.getLength(); i++) {
            org.w3c.dom.Node item = widgets.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element e = (Element) item;
            int id = Integer.valueOf(e.getAttribute("id"));
            String widgetType = e.getAttribute("type");
            if (widgetType.equals("State")) {
                Element stateAttributes = (Element) e.getElementsByTagName("attributes").item(0);
                String name = stateAttributes.getElementsByTagName("name").item(0).getTextContent();
                String stateType = stateAttributes.getElementsByTagName("type").item(0).getTextContent();

                List<Integer> incoming = new ArrayList<>();
                NodeList incomingList = stateAttributes.getElementsByTagName("incoming");
                for (int incIdx = 0; incIdx < incomingList.getLength(); incIdx++) {
                    String incId = ((Element) incomingList.item(incIdx)).getAttribute("id");
                    incoming.add(Integer.valueOf(incId));
                }

                List<Integer> outgoing = new ArrayList<>();
                NodeList outgoingList = stateAttributes.getElementsByTagName("outgoing");
                for (int outIdx = 0; outIdx < outgoingList.getLength(); outIdx++) {
                    String outId = ((Element) outgoingList.item(outIdx)).getAttribute("id");
                    outgoing.add(Integer.valueOf(outId));
                }

                State state = new State(id, name, Integer.valueOf(stateType), incoming, outgoing);
                states.add(state);
            } else if (widgetType.equals("Transition")) {
                Element stateAttributes = (Element) e.getElementsByTagName("attributes").item(0);
                Element eventElement = (Element) stateAttributes.getElementsByTagName("event").item(0);
                String event = eventElement.getAttribute("name");
                String code = stateAttributes.getElementsByTagName("code").item(0).getTextContent();
                String guard = stateAttributes.getElementsByTagName("guard").item(0).getTextContent();

                List<String> actions = new ArrayList<>();
                NodeList actionsList = stateAttributes.getElementsByTagName("action");
                for (int actIdx = 0; actIdx < actionsList.getLength(); actIdx++) {
                    Element actionElement = (Element) actionsList.item(actIdx);
                    String actionName = actionElement.getAttribute("name");
                    actions.add(actionName);
                }

                Transition transition = new Transition(id, event, guard, actions, code);
                transitions.add(transition);
            } else {
                throw new IllegalStateException("Unknown widget type: " + widgetType);
            }
        }

        return new Automaton(events, states, transitions);
    }
}
