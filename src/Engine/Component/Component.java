package Engine.Component;

import Engine.Core.Console;
import Engine.Core.GameObject;
import com.google.gson.annotations.Expose;
import imgui.ImGui;
import imgui.type.ImString;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public abstract class Component {

    public transient GameObject gameObject;
    public boolean isActive = true;

    public void drawImGUI(){
        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Class<?> type = field.getType();
                Object value = field.get(this);

                if (type == float.class) {
                    float[] fVal = new float[] { (float) value };
                    if (ImGui.dragFloat(field.getName(), fVal)) {
                        field.set(this, fVal[0]);
                    }
                }
                else if (type == int.class || type == Integer.class) {
                    int ival = field.getInt(field);            // get int value from the field
                    int[] iVal = new int[] { ival };         // wrap in array for ImGui
                    if (ImGui.dragInt(field.getName(), iVal)) {
                        field.setInt(field, iVal[0]);           // set back the new value
                    }
                }
                else if (type == boolean.class) {
                    boolean b = (boolean) value;
                    if (ImGui.checkbox(field.getName(), b)) {
                        field.set(this, !b);
                    }
                }
                else if (type == Vector2f.class) {
                    Vector2f vec = (Vector2f) value;
                    float[] vec2 = new float[] { vec.x, vec.y };
                    if (ImGui.dragFloat2(field.getName(), vec2)) {
                        vec.set(vec2[0], vec2[1]);
                    }
                }
                else if (type == String.class) {
                    ImString strVal = new ImString((String) value, 256);
                    if (ImGui.inputText(field.getName(), strVal)) {
                        field.set(this, strVal.get());
                    }
                }
                else if (type == Vector3f.class) {
                    Vector3f vec = (Vector3f) value;
                    float[] vec3 = new float[] { vec.x, vec.y , vec.z};
                    if (ImGui.dragFloat3(field.getName(), vec3)) {
                        vec.set(vec3[0], vec3[1], vec3[2]);
                    }
                }
                else if (List.class.isAssignableFrom(type)) {
                    ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                    Class<?> listElementType = (Class<?>) genericType.getActualTypeArguments()[0];
                    List<?> list = (List<?>) value;

                    ImGui.separator();
                    ImGui.text(field.getName() + " (" + list.size() + ")");

                    for (int i = 0; i < list.size(); i++) {
                        Object item = list.get(i);
                        ImGui.pushID(i); // isolate ImGui ID scope

                        // Handle primitives and common types
                        if (item instanceof Float) {
                            float[] f = { (Float) item };
                            if (ImGui.dragFloat("[" + i + "]", f)) {
                                ((List<Float>) list).set(i, f[0]);
                            }
                        } else if (item instanceof Integer) {
                            int[] n = { (Integer) item };
                            if (ImGui.dragInt("[" + i + "]", n)) {
                                ((List<Integer>) list).set(i, n[0]);
                            }
                        } else if (item instanceof Boolean) {
                            boolean b = (Boolean) item;
                            if (ImGui.checkbox("[" + i + "]", b)) {
                                ((List<Boolean>) list).set(i, !b);
                            }
                        } else if (item instanceof String) {
                            ImString str = new ImString((String) item, 256);
                            if (ImGui.inputText("[" + i + "]", str)) {
                                ((List<String>) list).set(i, str.get());
                            }
                        } else if (item instanceof Vector2f) {
                            Vector2f v = (Vector2f) item;
                            float[] f = new float[]{ v.x, v.y };
                            if (ImGui.dragFloat2("[" + i + "]", f)) {
                                v.set(f[0], f[1]);
                            }
                        } else if (item instanceof Vector3f) {
                            Vector3f v = (Vector3f) item;
                            float[] f = new float[]{ v.x, v.y, v.z };
                            if (ImGui.dragFloat3("[" + i + "]", f)) {
                                v.set(f[0], f[1], f[2]);
                            }
                        } else if (item != null) {
                            ImGui.text("Unsupported type: " + item.getClass().getSimpleName());
                        }

                        ImGui.popID();
                    }
                }
            } catch (IllegalAccessException e) {
                Console.error(e.getMessage());
            }
        }
    }
    public void update(float deltaTime){}
    public void start() {}
    public void cleanup() {
        // Default cleanup if needed
    }
}
