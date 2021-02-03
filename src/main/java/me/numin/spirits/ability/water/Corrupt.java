package me.numin.spirits.ability.water;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import me.numin.spirits.Spirits;
import me.numin.spirits.utilities.Methods;
import me.numin.spirits.utilities.Removal;
import me.numin.spirits.utilities.SpiritElement;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Corrupt extends WaterAbility implements AddonAbility {

  public LivingEntity target;
  public Location travelLoc = null;
  public static Set<Integer> heldEntities = new HashSet<Integer>();
  private Removal removal;
  private Timer timer = new Timer();

  private boolean charged = false;
  private boolean setElement;
  private boolean hasReached = true;
  private double range;
  private int ticks, chargeTicks;
  private long duration, cooldown, time;

  public Corrupt(Player player) {
    super(player);
    if (!bPlayer.canBend(this)) {
      return;
    }
    setFields();

    Entity targetEntity = GeneralMethods.getTargetedEntity(player, range);
    if (targetEntity instanceof LivingEntity) {
      this.target = (LivingEntity) targetEntity;
      heldEntities.add(target.getEntityId());
      time = System.currentTimeMillis();
      start();
    }
  }

  private void setFields() {
    this.cooldown = Spirits.plugin.getConfig().getLong("Abilities.Spirits.Water.Corrupt.Cooldown");
    this.duration = Spirits.plugin.getConfig().getLong("Abilities.Spirits.Water.Corrupt.Duration");
    this.range = Spirits.plugin.getConfig().getDouble("Abilities.Spirits.Water.Corrupt.Range");
    this.setElement = Spirits.plugin.getConfig().getBoolean("Abilities.Spirits.Water.Corrupt.SetElement");
    this.removal = new Removal(player, true);
  }

  @Override
  public void remove() {
    super.remove();

    if (target != null) {
      heldEntities.remove(target.getEntityId());
    }
  }

  @Override
  public long getCooldown() {
    return cooldown;
  }

  @Override
  public Location getLocation() {
    return null;
  }

  @Override
  public String getName() {
    return "Corrupt";
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
  public void progress() {
    if (removal.stop() || player.getLocation().distance(target.getLocation()) > range || !bPlayer.canBendIgnoreCooldowns(this)) {
      remove();
      bPlayer.addCooldown(this);
      return;
    }

    if (!charged && !player.isSneaking()) {
      remove();
      bPlayer.addCooldown(this);
      return;
    }

    if (System.currentTimeMillis() - time > duration) {
      remove();
      bPlayer.addCooldown(this);
      return;
    }

    if (player.isSneaking() && target != null && !target.isDead() && target.getWorld().equals(player.getWorld())) {

      if (System.currentTimeMillis() - time > 10000L) { // charge time is 10 seconds
        charged = true;
      }

      if (charged) {
        MovementHandler mh = new MovementHandler(player, this);
        mh.stopWithDuration(5, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "* READY *");
        createConvertSpirals();
        if (target instanceof Player && setElement) {
          BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) target);
          if (bPlayer.hasElement(SpiritElement.LIGHT_SPIRIT)) {
            bPlayer.addElement(SpiritElement.DARK_SPIRIT);
            bPlayer.getElements().remove(SpiritElement.LIGHT_SPIRIT);
            GeneralMethods.saveElements(bPlayer);
            player.sendMessage(ChatColor.ITALIC + "" + ChatColor.LIGHT_PURPLE
                + "You have successfully corrupted the " + ChatColor.BOLD + "" + SpiritElement.LIGHT_SPIRIT.getColor() + "Light Spirit.");
            target.sendMessage(ChatColor.ITALIC + "" + ChatColor.LIGHT_PURPLE
                + "You are now a" + ChatColor.BOLD + "" + SpiritElement.DARK_SPIRIT.getColor() + " DarkSpirit");
            ParticleEffect.SPELL_WITCH.display(target.getLocation(), 3, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.0);
          } else {
            baseEffect();
          }
        } else {
          baseEffect();
        }
        timer.schedule(new TimerTask() {
          @Override
          public void run() {
            charged = false;
          }
        }, 5000L);
      }
      handleSpirals();

    }
  }

  private void handleSpirals() {
    if (travelLoc == null && this.getStartTime() + duration < System.currentTimeMillis()) {
      remove();
      bPlayer.addCooldown(this);
      travelLoc = player.getEyeLocation();
    } else if (travelLoc == null) {
      ticks++;
      long chargingTime = System.currentTimeMillis() - getStartTime();
      this.chargeTicks = (int) (chargingTime / 25);
      if (!charged) {
        createWaterSpirals();
      } else {
        createConvertSpirals();
      }
      for (int i = -180; i < 180; i += 10) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 300, 128));
        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 300, 128));
      }
    }
  }

  private void baseEffect() {
    DamageHandler.damageEntity(target, 7, this);
    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 300, 2));
    ParticleEffect.SPELL_WITCH.display(target.getLocation(), 3, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.0);
  }

  public void paralyze(Entity entity) {
    if (entity instanceof Creature) {
      ((Creature) entity).setTarget(null);
    }

    if (entity instanceof Player) {
      if (Suffocate.isChannelingSphere((Player) entity)) {
        Suffocate.remove((Player) entity);
      }
    }
    MovementHandler mh = new MovementHandler((LivingEntity) entity, this);
    mh.stop(ChatColor.DARK_PURPLE + "* CORRUPTING *");
  }

  private void createWaterSpirals() {
    if (hasReached) {
      int amount = chargeTicks + 2;
      double maxHeight = 4;
      double distanceFromPlayer = 1.5;

      int angle = 5 * amount + 5 * ticks;
      double x = Math.cos(Math.toRadians(angle)) * distanceFromPlayer;
      double z = Math.sin(Math.toRadians(angle)) * distanceFromPlayer;
      double height = (amount * 0.10) % maxHeight;
      Location displayLoc = target.getLocation().clone().add(x, height, z);

      int angle2 = 5 * amount + 180 + 5 * ticks;
      double x2 = Math.cos(Math.toRadians(angle2)) * distanceFromPlayer;
      double z2 = Math.sin(Math.toRadians(angle2)) * distanceFromPlayer;
      Location displayLoc2 = target.getLocation().clone().add(x2, height, z2);
      GeneralMethods.displayColoredParticle("42aaf4", displayLoc2);
      GeneralMethods.displayColoredParticle("42aaf4", displayLoc);
      GeneralMethods.displayColoredParticle("70ddff", displayLoc2);
      GeneralMethods.displayColoredParticle("70ddff", displayLoc);
//      ParticleEffect.SPELL_MOB.display(displayLoc2, 1);
//      ParticleEffect.SPELL_MOB.display(displayLoc, 1);
    }
  }

  private void createConvertSpirals() {
    if (hasReached) {
      int amount = chargeTicks + 2;
      double maxHeight = 4;
      double distanceFromPlayer = 1.5;

      int angle = 5 * amount + 5 * ticks;
      double x = Math.cos(Math.toRadians(angle)) * distanceFromPlayer;
      double z = Math.sin(Math.toRadians(angle)) * distanceFromPlayer;
      double height = (amount * 0.10) % maxHeight;
      Location displayLoc = target.getLocation().clone().add(x, height, z);

      int angle2 = 5 * amount + 180 + 5 * ticks;
      double x2 = Math.cos(Math.toRadians(angle2)) * distanceFromPlayer;
      double z2 = Math.sin(Math.toRadians(angle2)) * distanceFromPlayer;
      Location displayLoc2 = target.getLocation().clone().add(x2, height, z2);
      GeneralMethods.displayColoredParticle("b02cc1", displayLoc2);
      GeneralMethods.displayColoredParticle("b02cc1", displayLoc);
      GeneralMethods.displayColoredParticle("893ac9", displayLoc2);
      GeneralMethods.displayColoredParticle("893ac9", displayLoc);
    }
  }

  @Override
  public String getAuthor() {
    return "Prride & Svetroid";
  }

  @Override
  public String getDescription() {
    return Spirits.plugin.getConfig().getString("Language.Abilities.Water.Corrupt.Description");
  }

  @Override
  public String getInstructions() {
    return Spirits.plugin.getConfig().getString("Language.Abilities.Water.Corrupt.Instructions");
  }

  @Override
  public String getVersion() {
    return Methods.getVersion();
  }

  @Override
  public boolean isEnabled() {
    return Spirits.plugin.getConfig().getBoolean("Abilities.Spirits.Water.Corrupt.Enabled");
  }

  @Override
  public void load() {
  }

  @Override
  public void stop() {
  }

}
