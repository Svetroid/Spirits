package me.numin.spirits.ability.spirit;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.numin.spirits.Spirits;
import me.numin.spirits.Methods;
import me.numin.spirits.Methods.SpiritType;
import me.numin.spirits.ability.api.SpiritAbility;

public class Vanish extends SpiritAbility implements AddonAbility {

    private long cooldown;
    private long time;
    private long chargeTime;
    private long duration;
    private boolean removeFire;
    private boolean isCharged;
    private Location origin;
    private long range;
    private long radius;
    private boolean applyInvis = true;
    private int particleFrequency;

    public Vanish(Player player) {
        super(player);

        if (!bPlayer.canBend(this)) {
            return;
        }
        setFields();
        time = System.currentTimeMillis();
        start();
    }

    private void setFields() {
        this.cooldown = Spirits.plugin.getConfig().getLong("Abilities.Spirits.Neutral.Vanish.Cooldown");
        this.duration = Spirits.plugin.getConfig().getLong("Abilities.Spirits.Neutral.Vanish.Duration");
        this.chargeTime = Spirits.plugin.getConfig().getLong("Abilities.Spirits.Neutral.Vanish.ChargeTime");
        this.radius = Spirits.plugin.getConfig().getLong("Abilities.Spirits.Neutral.Vanish.Radius");
        this.particleFrequency = Spirits.plugin.getConfig().getInt("Abilities.Spirits.Neutral.Vanish.ParticleFrequency");
        this.removeFire = Spirits.plugin.getConfig().getBoolean("Abilities.Spirits.Neutral.Vanish.RemoveFire");

        boolean doHalfEffect = Spirits.plugin.getConfig().getBoolean("Abilities.Spirits.Neutral.Vanish.DivideRange.Enabled");
        double healthReq = Spirits.plugin.getConfig().getDouble("Abilities.Spirits.Neutral.Vanish.DivideRange.HealthRequired");
        int divideFactor = Spirits.plugin.getConfig().getInt("Abilities.Spirits.Neutral.Vanish.DivideRange.DivideFactor");

        if (doHalfEffect && player.getHealth() < healthReq) {
            this.range = Spirits.plugin.getConfig().getLong("Abilities.Spirits.Neutral.Vanish.Range") / divideFactor;
        } else {
            this.range = Spirits.plugin.getConfig().getLong("Abilities.Spirits.Neutral.Vanish.Range");
        }

        this.origin = player.getLocation();
        this.isCharged = false;

    }

    @Override
    public void progress() {
        if (player.isDead() || !player.isOnline() || GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
            remove();
            return;
        }

        if (!isCharged) {
            if (player.isSneaking()) {
                if (System.currentTimeMillis() > time + chargeTime) {
                    isCharged = true;
                } else {
                    if (new Random().nextInt(particleFrequency) == 0) {
                        ParticleEffect.DRAGON_BREATH.display(player.getLocation().add(0, 1, 0), 0, 0, 0, 0.09F, 1);
                    }
                }
            } else {
                remove();
            }
        } else {
            if (player.isSneaking()) {
                playEffects();

                if ((origin.distanceSquared(player.getLocation()) > radius * radius) || (System.currentTimeMillis() > time + duration)) {
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5F, -1);
                    remove();
                }
            } else {
                vanishPlayer();
                Location targetLoc = GeneralMethods.getTargetedLocation(player, range);
                player.teleport(targetLoc);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5F, -1);
                remove();
            }
        }
    }

    private void vanishPlayer() {
        if (removeFire) {
            player.setFireTicks(-1);
        }
        bPlayer.addCooldown(this);
    }

    private void playEffects() {
        if (new Random().nextInt(particleFrequency) == 0) {
            Methods.playSpiritParticles(player, player.getLocation().add(0, 1, 0), 0.5, 0.5, 0.5, 0, 1);
        }
        if (applyInvis) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) duration, 2), true);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5F, -1);
            Methods.animateVanish(player);
            applyInvis = false;
        }
    }

    @Override
    public void remove() {
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
        if (isCharged) Methods.animateVanish(player);
        super.remove();
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public Location getLocation() {
        return player.getLocation();
    }

    @Override
    public String getName() {
        return "Vanish";
    }

    @Override
    public String getDescription() {
        return Methods.setSpiritDescription(SpiritType.NEUTRAL, "Mobility") +
                Spirits.plugin.getConfig().getString("Language.Abilities.Spirit.Vanish.Description");
    }

    @Override
    public String getInstructions() {
        return Methods.getSpiritColor(SpiritType.NEUTRAL) + Spirits.plugin.getConfig().getString("Language.Abilities.Spirit.Vanish.Instructions");
    }

    @Override
    public String getAuthor() {
        return Methods.getSpiritColor(SpiritType.NEUTRAL) + "" + Methods.getAuthor();
    }

    @Override
    public String getVersion() {
        return Methods.getSpiritColor(SpiritType.NEUTRAL) + Methods.getVersion();
    }

    @Override
    public boolean isEnabled() {
        return Spirits.plugin.getConfig().getBoolean("Abilities.Spirits.Neutral.Vanish.Enabled");
    }

    @Override
    public boolean isExplosiveAbility() {
        return false;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public boolean isIgniteAbility() {
        return false;
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public void load() {}
    @Override
    public void stop() {}
}