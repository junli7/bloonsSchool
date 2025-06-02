# **Italian Brainrot Tower Defense** 
![banner](https://myoctocat.com/assets/images/base-octocat.svg)

This project is a simple tower defense game. It combines aspects of **BloonsTD** with **italian brainrot**. main objective of the game is to survive as many rounds as possible by placing and upgrading italian brainrot characters. enemies are different variants of humans attempting to traverse the path.

The <ins> GamePanel</ins> class handles visuals (drawing), user input, and the game loop. This includes movement of enemies (humans), monkeys (characters) and wave management. 

The <ins> GameState</ins> class as suggested in the name, manages the state of the game. This includes the money, lives, and wave number. 

The <ins> Monkey</ins> class creates the characters to be placed and manages the upgrading stats and attacking. <ins> MonkeyB</ins> and <ins> MonkeyC</ins> are both child classes of Monkey with specilized abilities: (Exposives and Slowing Projectiles respectively).

<ins> Human</ins> objects are the enemies moving along the path and this movement is handled by the <ins> Map</ins> class. 

<ins> Projectile</ins> represents the objects (projectiles) fired by all monkeys. 

The <ins> SideInfoPanel</ins> class handles the rightmost UI panel, showing general information such as money, wave number, and the characters.

The <ins> UpgradeControlPanel</ins> class inclues a pop-up, allowing for the characters to be upgraded. 

The <ins> SpriteManager</ins> handles loading the images as well as resizing them.

<ins> Mainframe</ins> is the "main" class. This is the class to run to start the application and sets up the main game window and panels.


## Variables Used

This section explains all important variables used across all classes.
‎
**GamePanel Class Variables:**

```
defaultMonkey (String): constant string identifier for the default monkey type

bomberMonkey (String): constant string identifier for the bomber monkey type

slowMonkey (String): constant string identifier for the slow monkey type

monkeys (List): list to store all active Monkey objects

humans (List): list to store all active Human objects

selectedMonkey (Monkey): refers to the current selected Monkey (used for upgrading and selling)

upgradeControlPanelRef (UpgradeControlPanel): refers to the UI panel for upgrades

gameState (GameState): refers to the GameState object managing game-wide parameters

map (Map): gamemap, defining a path for humans

random (Random): Random object used for procedural generation

waveDefinitions (List): list of defined wave configurations

isPlacingMonkey (boolean): Boolean indicating if the user is currently in monkey placement mode

placingMonkeyType (String): type of monkey being placed

placementPreviewMonkey (Monkey): temporary monkey shown before placement to indicate position and range

placementValid (boolean): Boolean indicating if the current monkey placement position is valid

placementMouseX, placementMouseY (int): Current mouse coordinates during monkey placement

separationBuffer (double): buffer distance used to ensure monkeys are not placed too close to each other or the path

gameIsOver (boolean): Boolean indicating if the game has ended

restartButtonBounds (Rectangle): Defines the bounds of the restart game button

restartButtonHoverColor (Color): color of the restart button when hovered over.

currentSpawnCooldownTicks (int): countdown for the delay between enemy spawns.

currentProceduralWaveDefinition (WaveDefinition): defines a procedurally generated wave.

lastProceduralWaveGenerated (int): stores the wave number for which the last procedural wave was generated.

```
**GameState Class Variables:**
```

money (int):  amount of money the player has

bloonsKilled (int): total number of enemies killed by player

currentWave (int): current wave number

lives (int): number of lives left

initialLives (int): starting number of lives for the game

initialMoney (int): starting amount of money for the game
```
**Human Class Variables:**
```
x, y (double): current coordinates of the human on the map

currentSpeed (double): current movement speed of the human

originalSpeed (double): base movement speed of the human

hitboxDiameter (int): diameter of the humans collision box

health (int): current health points of the human

currentPathIndex (int): index of the current waypoint the human is moving towards

camo (boolean): boolean for if the human is camo (invisible to non-camo detecting monkeys)

reachedEnd (boolean): boolean indicating if the human has reached the end of the path

sprite (BufferedImage): image used to draw the human

currentAngleRadians (double): current rotation angle of the human sprite

humanType (String): specific type of human (ex. "human_baby)

moneyReward (int): amount of money rewarded to the player when human is killed

slowEffectEndTimeMillis (long): time when any slow effect on the human will end

slowRate (double): multiplier for slowing down human speed

random (Random): Random object for selecting sprites

currentSpriteVariants (List): list of possible sprite paths for this human type

defaultCamoForType (boolean): boolean for if this human type is camo by default
```

Map Class Variables:
```
pathCoordinates (ArrayList): list of Point objects defining the points of the path for the enemies to follow.
```
Monkey Class Variables:
```
x, y (int): Current coordinates of the monkey on the map.

range (double): radius within which the monkey can detect and target humans.

hitbox (double): diameter of the monkey's collision box for placement.

upgradeCost (int): cost to upgrade the monkey to the next level.

level (int): current level of the monkey.

projectiles (List): list of projectiles currently fired by this monkey.

monkeyColor (Color): base color used for drawing the monkey if no sprite is available.

COST (int): Static constant for the base cost of a default monkey.

DEFAULT_INITIAL_RANGE (double): Static constant for the initial range of a default monkey.

DEFAULT_INITIAL_HITBOX (double): Static constant for the initial hitbox of a default monkey.

projectileColor (Color): Color of the projectiles fired by this monkey.

projectileRadius (int): Radius of the projectiles.

projectileSpeed (double): Speed of the projectiles.

projectileDamage (int): Damage dealt by the projectiles.

projectileIsExplosive (boolean): True if the projectile explodes on impact.

projectileAoeRadius (double): Radius of the Area of Effect (AoE) for explosive projectiles.

projectileExplosionVisualColor (Color): Color of the visual explosion effect.

projectileExplosionVisualDuration (int): Duration of the visual explosion effect in game ticks.

projectileExplosionSpritePath (String): Path to the sprite for the explosion visual.

lastShotTime (long): Timestamp of the last time the monkey fired a projectile.

shootCooldown (long): Minimum time (in milliseconds) between shots.

isSelected (boolean): boolean indicating if the monkey is currently selected by the user.

canSeeCamo (boolean): True if the monkey can detect camo humans.

idleSprite (BufferedImage): image used when the monkey is idle.

shootingSprite (BufferedImage): image used when the monkey is shooting.

idleSpritePath (String): file path for the idle sprite.

shootingSpritePath (String): file path for the shooting sprite.

isAnimatingShot (boolean): boolean indicating if the monkey is currently playing a shooting animation.

shotAnimationEndTime (long): Timestamp when the shooting animation should end.

SHOT_ANIMATION_DURATION_MS (long): Duration of the shooting animation.

lastShotAngleRadians (double): angle at which the last shot was fired, used for sprite rotation.

archetypeNONE (String): constant for no chosen archetype.

archetypeDART_SNIPER (String): constant for Dart Monkey Sniper archetype.

archetypeDART_QUICKFIRE (String): constant for Dart Monkey Quickfire archetype.

archetypeBOMB_FRAGS (String): constant for Bomb Monkey Frags archetype.

archetypeBOMB_CONCUSSION (String): constant for Bomb Monkey Concussion archetype.

archetypeICE_PERMAFROST (String): constant for Ice Monkey Permafrost archetype.

archetypeICE_BRITTLE (String): constant for Ice Monkey Brittle archetype.

chosenArchetype (String): identifier for the chosen upgrade path/archetype.

hasChosenArchetype (boolean): boolean indicating if an archetype has been selected.

totalSpentOnMonkey (int): Total money spent on purchasing and upgrading this monkey.

sellPercentage (double): percentage of total spent money that is returned when selling a monkey.

MonkeyB Class Variables:

COST (int): Static constant for the base cost of a Bomber Monkey.

monkeyBInitialRange (double): Static constant for the initial range of a Bomber Monkey.

monkeyBInitialHitbox (double): Static constant for the initial hitbox of a Bomber Monkey.

monkeyBIDLEspritePath (String): file path for the Bomber Monkey's idle sprite.

monkeyBSHOOTspritePath (String): file path for the Bomber Monkey's shooting sprite.

explosionSprite (String): file path for the explosion effect sprite.

MonkeyC Class Variables:

COST (int): Static constant for the base cost of a Slow Monkey.

monkeyCIDLEspritePath (String): file path for the Slow Monkey's idle sprite.

monkeyCSHOOTspritePath (String): file path for the Slow Monkey's shooting sprite.

iceExplosionspritePath (String): file path for the ice explosion sprite.

slowDurationMillis (int): duration (in milliseconds) for which humans are slowed by this monkey's projectiles.
```
Projectile Class Variables:
```
x, y (double): Current coordinates of the projectile.

dx, dy (double): Change in X and Y coordinates per update, representing movement direction.

radius (int): Visual radius of the projectile.

color (Color): Color of the projectile.

speed (double): Movement speed of the projectile.

damage (int): Damage dealt by the projectile.

target (Human): Human object this projectile is targeting.

isExplosive (boolean): True if the projectile explodes on impact.

aoeRadius (double): Radius of the Area of Effect (AoE) for explosive projectiles.

isSlowingProjectile (boolean): True if the projectile applies a slow effect.

slowDurationMillis (int): Duration of the slow effect applied by this projectile.

currentState (enum State): Current state of the projectile (active, exploding, spent).

explosionAgeTicks (int): Counter for how long the explosion visual has been active.

explosionDurationTicks (int): Total duration of the explosion visual.

explosionCenterX, explosionCenterY (double): Center coordinates of the explosion.

explosionSprite (BufferedImage): image used for the explosion effect.

sprite (BufferedImage): image used for the projectile itself while flying.

defaultSpritePath (String): Default sprite path for non-explosive, non-slowing projectiles.

bombSpritePath (String): Sprite path for explosive projectiles.

slowSpritePath (String): Sprite path for slowing projectiles.

hitThreshold (double): Distance threshold to consider a projectile to have hit its target.

SideInfoPanel Class Variables:

gameState (GameState): refers to the GameState object.

gamePanel (GamePanel): refers to the GamePanel object.

panelWidth (int): Static constant for the preferred width of the panel.

buttonHeight (int): Static constant for the height of buttons.

buySpritePreviewSize (int): Size for the preview sprites on buy buttons.

buyButtonWidth (int): Width for the buy buttons.

defaultButtonColor (Color): Default background color for buttons.

hoverButtonColor (Color): Background color for buttons when hovered over.

pressedButtonColor (Color): Background color for buttons when pressed.

disabledButtonColor (Color): Background color for disabled buttons.

buttonTextColor (Color): Text color for buttons.

waveStatusLabel (JLabel): Label displaying the current wave status.

startGameButton (JButton): Button to start the game or the next wave.

livesLabel (JLabel): Label displaying the current number of lives.
```
SpriteManager Class Variables:
```
spriteCache (Map<String, BufferedImage>): cache to store loaded sprites, preventing redundant loading.

placeholderSprite (BufferedImage): placeholder image used when a requested sprite cannot be loaded.
```
UpgradeControlPanel Class Variables:
```
upgradeGUI_instance (UpgradeGUI): An instance of UpgradeGUI to handle the drawing and logic of the upgrade interface.

currentSelectedMonkey (Monkey): monkey currently selected by the user, whose upgrade options are displayed.

gameState (GameState): refers to the GameState object.

gamePanel_ref (GamePanel): refers to the GamePanel object, used for selling monkeys or repainting.

panelWidth (int): Static constant for the preferred width of the panel.
```
UpgradeGUI Class Variables:
```
actionNONE, actionUpgradedOrArchetypeChosen, actionSOLD (int): constants representing different actions resulting from a click.

upgradeButtonBounds (Rectangle): Defines the clickable area for the standard upgrade button.

archetype1ButtonBounds (Rectangle): Defines the clickable area for the first archetype button.

archetype2ButtonBounds (Rectangle): Defines the clickable area for the second archetype button.

sellButtonBounds (Rectangle): Defines the clickable area for the sell button.

buttonWidth, archetypebuttonWidth, sellbuttonWidth (int): Widths for different types of buttons.

buttonHeight, archetypebuttonHeight, sellbuttonHeight (int): Heights for different types of buttons.

padding (int): General spacing used for layout.

sectionSpacing (int): Vertical spacing between different sections of the GUI.

tooltipSpacingAfterSell (int): Vertical spacing specifically after the sell button to place the tooltip.

defaultButtonColor, hoverButtonColor, disabledButtonColor, disabledHoverButtonColor (Color): Color schemes for upgrade/archetype buttons.

defaultSellButtonColor, hoverSellButtonColor (Color): Color schemes for the sell button.

currentButtonColor, currentArchetype1Color, currentArchetype2Color, currentSellButtonColor (Color): Variables to store the dynamically updated colors of the buttons based on hover and affordability.

buttonBorderColor (Color): Color for button borders.

buttonTextColor (Color): Text color for buttons.

buttonFont, headerFont, statsFont, costFont, tooltipFont, sellButtonFont (Font): Different font styles used for text elements.

currentTooltipText (String): text displayed as a tooltip when hovering over a button.
```
SpawnInstruction Inner Class Variables (within GamePanel):
```
humanType (String): type of human (enemy) to spawn (e.g., "normal", "kid", "bossbaby").

count (int): number of units of this type to spawn.

delayTicksAfterPreviousGroup (int): Delay in game ticks before this group starts spawning, after the previous group.

intervalTicksPerUnit (int): Delay in game ticks between spawning individual units within this group.

isCamo (boolean): Indicates if the human type is camo.
```
WaveDefinition Inner Class Variables (within GamePanel):
```
spawns (List): list of SpawnInstruction objects defining the composition of the wave.
```

Types of Methods

This section categorizes and briefly describes the types of methods implemented across the project's classes.
Constructor Methods:

    Initialize new objects of a class, setting up their initial state and dependencies.

Game Logic and Update Methods:

    Implement the core game mechanics, including movement, targeting, attacking, and state transitions. These methods are typically called repeatedly within the game loop.

Drawing and Rendering Methods:

    Responsible for drawing the visual elements of the game onto the screen. This includes paintComponent overrides and helper methods for drawing specific UI elements or game entities.

Event Handling Methods:

    Respond to user input, such as mouse clicks and movements, to trigger game actions. These methods often involve MouseListener and MouseMotionListener implementations.

State Management and Mutator Methods:

    Modify the internal state of objects or the game, often related to resources, health, or progression.

Accessor (Getter) Methods:

    Provide read-only access to the internal state or properties of an object.

Utility and Helper Methods:

    Perform specific tasks that support the main logic, such as calculations, resource loading, specific object creation, or general-purpose computations.

Upgrade and Archetype Methods:

    Manage the progression and specialization of game entities, particularly monkeys. This includes applying stat changes and handling the selection of upgrade paths.

UI Component Styling and Creation Methods:

    Methods specifically designed to style and create Swing UI components, often involving setting colors, fonts, borders, and layout properties.


Rationale

The project is structured using Swing for the graphical user interface, leveraging JPanel for the main game display and Timer for the game loop. MainFrame acts as the primary application window, orchestrating the various panels. Separation of concerns is achieved by having GamePanel manage rendering and interaction, while GameState encapsulates the core game variables and their manipulation. Human and Monkey classes define the core entities with their own update and draw logic. Map class provides the pathfinding data. This modular design aims for maintainability and clarity. use of MouseAdapter and MouseMotionAdapter simplifies event handling for user input. Wave management is designed to support both pre-defined and procedurally generated waves, allowing for extensible gameplay. Monkeys have a clear upgrade path system with archetypes to introduce strategic depth, and specialized monkey types like MonkeyB and MonkeyC extend the base functionality. Projectile class handles the behavior of shots fired by monkeys. Dedicated UI panels (SideInfoPanel, UpgradeControlPanel) improve user experience by providing clear information and interactive elements. SpriteManager centralizes image loading and caching for efficient resource management. UpgradeGUI specifically encapsulates the visual and interaction logic for the upgrade interface, making UpgradeControlPanel cleaner.
Dependencies

This project is written in Java and relies on the following standard Java libraries:

    javax.swing.*: For GUI components and event handling.

    java.awt.*: For graphics, drawing, and basic UI elements (e.g., Color, Point, Rectangle, Graphics2D, Font, FontMetrics, BasicStroke, AlphaComposite, Composite, Image, MouseInfo).

    java.awt.event.*: For event handling (mouse, action).

    java.awt.image.BufferedImage: For image handling.

    java.awt.geom.AffineTransform: For applying transformations like rotation to graphics.

    java.awt.geom.Line2D.Double: For geometric calculations related to path segments.

    java.util.*: For data structures like ArrayList, List, Iterator, Random, HashMap, Map.

    java.io.IOException, java.io.InputStream: For input/output operations, particularly for image loading.

    javax.imageio.ImageIO: For reading image files.

Files Used

    MainFrame.java: main entry point of the application, responsible for setting up the JFrame and integrating all other panels.

    GamePanel.java: Contains the GamePanel class, responsible for game rendering, logic, and user interaction.

    GameState.java: Contains the GameState class, managing the core numerical state of the game.

    Human.java: Defines the Human class, representing enemy units with their movement, health, and visual properties.

    Map.java: Defines the Map class, managing the game's path for human movement.

    Monkey.java: Defines the Monkey class, the base class for all monkey towers, including their targeting, shooting, and upgrade logic.

    MonkeyB.java: Defines the MonkeyB class, a specialized monkey type that extends Monkey with explosive projectile capabilities.

    MonkeyC.java: Defines the MonkeyC class, a specialized monkey type that extends Monkey with slowing projectile capabilities.

    Projectile.java: Defines the Projectile class, representing shots fired by monkeys, handling their movement, impact, and visual effects.

    SideInfoPanel.java: Defines the SideInfoPanel class, displaying general game information (money, lives, wave) and buy buttons for monkeys.

    SpriteManager.java: Defines the SpriteManager class, responsible for loading, caching, and scaling image sprites.

    UpgradeControlPanel.java: Defines the UpgradeControlPanel class, providing a UI for upgrading and selling selected monkeys.

    UpgradeGUI.java: Defines the UpgradeGUI class, which handles the drawing and interaction logic for the monkey upgrade interface within the UpgradeControlPanel.

    map_background.png (Expected, based on usage): Image file for the game map background.

    human_baby.png, human_kid0.png, human_kid1.png, human_normal0.png, human_normal1.png, human_normal2.png, human_bodybuilder.png, human_businessman.png, human_bigbaby.png, human_ninja.png, human_bossninja.png (Expected, based on usage): Image files for various human sprites.

    monkey_base_idle.png, monkey_base_shoot.png (Expected, based on usage): Image files for base monkey sprites.

    monkey_bomber_idle.png, monkey_bomber_shoot.png (Expected, based on usage): Image files for Bomber Monkey sprites.

    monkey_slow_idle.png, monkey_slow_shoot.png (Expected, based on usage): Image files for Slow Monkey sprites.

    explosion_effect.png (Expected, based on usage): Image file for the explosion effect (used by MonkeyB).

    project_slow.png (Expected, based on usage): Image file for the slow projectile/explosion effect (used by MonkeyC and Projectile).

    projectile_dart.png (Expected, based on usage): Default image file for projectiles.

    projectile_bomb.png (Expected, based on usage): Image file for bomb projectiles.

Compilation and Execution

This project is a Java Swing application.

To Compile:
Navigate to the root directory of your project where all .java files are located in your terminal or command prompt.

javac *.java

This command will compile all Java source files in the current directory. If your classes are organized into packages, you will need to adjust the javac command accordingly (e.g., javac -d . src/*.java).

To Execute:
After successful compilation, run the application by executing the MainFrame class, which contains the main method.

java MainFrame

This will launch the game window.
