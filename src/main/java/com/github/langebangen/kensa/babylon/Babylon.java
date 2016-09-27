package com.github.langebangen.kensa.babylon;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Martin.
 */
@Singleton
public class Babylon
{
	private final Random random;
	private final List<String> dishes;

	@Inject
	private Babylon()
	{
		this.random = new Random();
		this.dishes = new ArrayList<String>();
		loadDishes();
	}

	public String getRandomDish()
	{
		int index = random.nextInt(dishes.size());
		return dishes.get(index);
	}

	private void loadDishes()
	{
		InputStream resourceAsStream = Babylon.class.getResourceAsStream("/babylon.txt");
		Scanner scanner = new Scanner(resourceAsStream);
		while(scanner.hasNextLine())
		{
			String dish = scanner.nextLine();
			String ingredients = scanner.nextLine();
			dishes.add(dish + "\n" + ingredients);
		}
	}
}
