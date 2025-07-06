package Tools;

import Engine.Component.Component;

import java.util.ArrayList;
import java.util.List;

public class ComponentRegistration {

    private static final List<Class <? extends Component>> RegisteredComponents = new ArrayList<>();

    public static void register(Class<? extends Component> clas){
        if(!RegisteredComponents.contains(clas)){
            RegisteredComponents.add(clas);
        }
    }

    public static List<Class <? extends  Component>> getRegisteredComponents(){
        return RegisteredComponents;
    }

    public static void Clear(){
        RegisteredComponents.clear();
    }
}
