package net.runelite.client.plugins.unethicalite;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.input.MouseManager;
import net.runelite.client.input.MouseWheelListener;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.unethicalite.regions.RegionHandler;
import net.runelite.client.plugins.unethicalite.ui.UnethicalitePanel;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.unethicalite.api.plugins.SettingsPlugin;
import net.unethicalite.client.config.UnethicaliteConfig;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;

@PluginDescriptor(
		name = "Devious Client",
		hidden = true
)
@Slf4j
public class UnethicalitePlugin extends SettingsPlugin
{

	@Inject
	private UnethicaliteConfig config;

	@Inject
	private EventBus eventBus;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private RegionHandler regionHandler;

	@Inject
	private ExecutorService executorService;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Client client;

	private UnethicalitePanel unethicalitePanel;
	private NavigationButton navButton;

	private MouseWheelListener menuScrollHandler = new MouseWheelListener() {
		@Override
		public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event)
		{
			if (client.isMenuOpen())
			{
				Rectangle menuBounds = new Rectangle(client.getMenuX(), client.getMenuY(), client.getMenuWidth(), client.getMenuHeight());
				Rectangle submenuBounds = new Rectangle(client.getSubmenuX(), client.getSubmenuY(), client.getSubmenuWidth(), client.getSubmenuHeight());
				if (submenuBounds.contains(event.getX(), event.getY()))
				{
					client.setSubmenuScroll(Math.max(0, client.getSubmenuScroll() + event.getWheelRotation()));
					event.consume();
				}
				else if (menuBounds.contains(event.getX(), event.getY()))
				{
					client.setMenuScroll(Math.max(0, client.getMenuScroll() + event.getWheelRotation()));
					event.consume();
				}
			}
			return event;
		}
	};

	@Override
	protected void startUp() throws Exception
	{
		eventBus.register(regionHandler);

		unethicalitePanel = new UnethicalitePanel(client, config, configManager);

		eventBus.register(unethicalitePanel);

		mouseManager.registerMouseWheelListener(0, menuScrollHandler);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "openosrs.png");

		navButton = NavigationButton.builder()
				.tooltip("Devious Client")
				.icon(icon)
				.priority(-1)
				.panel(unethicalitePanel)
				.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		mouseManager.unregisterMouseWheelListener(menuScrollHandler);
		clientToolbar.removeNavigation(navButton);
		eventBus.unregister(regionHandler);
		eventBus.unregister(unethicalitePanel);
	}

	@Override
	public Config getConfig()
	{
		return config;
	}

	@Override
	public String getPluginName()
	{
		return "Devious Client";
	}

	@Override
	public String getPluginDescription()
	{
		return "Devious Client Settings";
	}

	@Override
	public String[] getPluginTags()
	{
		return new String[]{"unethicalite"};
	}
}
