package com.github.langebangen.kensa.babylon;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Martin.
 */
public class Babylon
{
	public static final Babylon INSTANCE = new Babylon();

	private final Random random;
	private final List<String> dishes;

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
			System.out.println(dish);
			String ingredients = scanner.nextLine();
			System.out.println(ingredients);
			dishes.add(dish + "\n" + ingredients);
		}
	}
}
