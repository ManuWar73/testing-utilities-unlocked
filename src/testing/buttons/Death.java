package testing.buttons;

import arc.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import testing.*;
import testing.content.*;
import testing.ui.*;
import testing.util.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class Death extends TUButton{
    static float sTimer, cTimer;

    static Stack kill = new Stack(), dupe = new Stack();
    static Image unit1 = new Image(), unit2 = new Image(), knife = new Image(), plus = new Image();

    public static void init(){
        knife.setDrawable(TUIcons.seppuku);
        plus.setDrawable(TUIcons.clone);

        unit1.setScaling(Scaling.fit).setSize(TUIcons.seppuku.imageSize());
        unit2.setScaling(Scaling.fit).setSize(TUIcons.clone.imageSize());

        Events.run(Trigger.update, () -> {
            if(state.isGame()){
                Unit u = player.unit();
                unit1.setDrawable(u != null ? u.type.uiIcon : Icon.units.getRegion());
                unit2.setDrawable(u != null ? u.type.uiIcon : Icon.units.getRegion());
            }
        });
    }

    /** <i><b>SPONTANIUM COMBUSTUM!</b> That's a spell that makes the person who said it <b>e x p l o -</b></i> */
    public static void spontaniumCombustum(){
        Unit u = player.unit();
        if(net.client()){ //For 2r2t
            Utils.runCommand("die");
            killLightning();
        }else{
            boolean insta = settings.getBool("tu-instakill");
            if(input.shift()){
                if(insta){
                    Utils.copyJS("""
                        let u = Vars.player.unit();
                        u.elevation = 0;
                        u.health = -1;
                        u.dead = true;
                        u.kill();"""
                    );
                }else{
                    Utils.copyJS("Vars.player.unit().kill();");
                }
                return;
            }
            if(u != null){
                if(insta){
                    u.elevation(0);
                    u.health(-1);
                    u.dead(true);
                }
                u.kill();
                killLightning();
            }
        }
    }

    public static void killLightning(){
        Unit u = player.unit();
        if(u != null){
            for(int i = 0; i < Math.max(1f, u.hitSize / 4f); i++){
                TUFx.deathLightning.at(u, true);
            }
        }
    }

    public static void mitosis(){
        if(input.shift()){
            Utils.copyJS("""
                let u = Vars.player.unit();
                u.type.spawn(u.team, u).rotation = u.rotation;"""
            );
            return;
        }

        Unit u = player.unit();
        if(u != null){
            u.type.spawn(u.team, u).rotation(u.rotation);
            Fx.spawn.at(u);
        }
    }

    public static Cell<ImageButton> seppuku(Table t){
        Cell<ImageButton> i = t.button(Icon.units, TUStyles.tuRedImageStyle, TUVars.iconSize, () -> {
            if(sTimer > TUVars.longPress) return;
            spontaniumCombustum();
        }).growX();

        ImageButton b = i.get();
        TUElements.boxTooltip(b, "@tu-tooltip.button-seppuku");
        b.setDisabled(() -> player.unit() == null || player.unit().type.internal || TestUtils.disableServer());
        b.update(() -> {
            if(b.isPressed() && !b.isDisabled() && !net.client()){
                sTimer += Time.delta;
                if(sTimer > TUVars.longPress){
                    spontaniumCombustum();
                }
            }else{
                sTimer = 0f;
            }

            kill.clearChildren();
            kill.add(unit1);
            kill.add(knife);
            b.replaceImage(kill);
        });
        b.setColor(player.team().color != null ? player.team().color : TUVars.curTeam.color);

        return i;
    }

    public static Cell<ImageButton> clone(Table t){
        Cell<ImageButton> i = t.button(Icon.units, TUStyles.tuRedImageStyle, TUVars.iconSize, () -> {
            if(cTimer > TUVars.longPress) return;
            mitosis();
        }).growX();

        ImageButton b = i.get();
        TUElements.boxTooltip(b, "@tu-tooltip.button-clone");
        b.setDisabled(() -> player.unit() == null || player.unit().type.internal || TestUtils.disableButton());
        b.update(() -> {
            if(b.isPressed() && !b.isDisabled() && !net.client()){
                cTimer += Time.delta;
                if(cTimer > TUVars.longPress){
                    mitosis();
                }
            }else{
                cTimer = 0f;
            }

            b.setColor(player.team().color != null ? player.team().color : TUVars.curTeam.color);

            dupe.clearChildren();
            dupe.add(unit2);
            dupe.add(plus);
            b.replaceImage(dupe);
        });

        return i;
    }

    public void add(Table table){
        table.table(Tex.buttonEdge3, t -> {
            clone(t).size(TUVars.iconSize, TUVars.iconSize);
            seppuku(t).size(TUVars.iconSize, TUVars.iconSize);
        });
    }
}
