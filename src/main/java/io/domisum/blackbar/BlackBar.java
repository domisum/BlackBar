package io.domisum.blackbar;

import io.domisum.lib.auxiliumlib.PHR;
import io.domisum.lib.auxiliumlib.datacontainers.math.Coordinate2DInt;
import io.domisum.lib.auxiliumlib.util.StringUtil;
import io.domisum.lib.auxiliumlib.util.math.MathUtil;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Scanner;

public class BlackBar
{
	
	private static final String PAGE_LAYOUT_FORMAT = "<number of days per row>x<number of rows>";
	
	
	public static void main(String[] args)
	{
		var dateOfBirth = readInputDate("Date of birth:");
		int lifeExpectancyYears = readInputInteger("Life expectancy (years):");
		if(lifeExpectancyYears < 1)
			throw new IllegalArgumentException("Life expectancy (years) has to be at least 1");
		
		var expectedDateOfDeath = dateOfBirth.plusYears(lifeExpectancyYears);
		int expectedDaysAlive = (int) ChronoUnit.DAYS.between(dateOfBirth, expectedDateOfDeath);
		
		var dateToday = LocalDate.now();
		int daysLived = (int) ChronoUnit.DAYS.between(dateOfBirth, dateToday);
		double percentageDaysLivedOfExpectedDaysAlive = MathUtil.round(daysLived/(double) expectedDaysAlive*100, 1);
		
		System.out.println(PHR.r("Born on {} with a life expectancy of {} years, your estimated date of death is {}",
			dateOfBirth, lifeExpectancyYears, expectedDateOfDeath));
		System.out.println(PHR.r("Today's date is {}. Not including today, you have lived {} days of an expected {} days, which is {}%",
			dateToday, daysLived, expectedDaysAlive, percentageDaysLivedOfExpectedDaysAlive));
		System.out.println();
		
		var pageLayout = readPageLayout();
		int daysPerPage = pageLayout.getX()*pageLayout.getY();
		
		int totalPages = (int) Math.ceil(expectedDaysAlive/(double) daysPerPage);
		int blackPages = daysLived/daysPerPage;
		var pageInProgressDisplayOptional = getPageInProgressDisplay(daysLived, pageLayout);
		int whitePages = totalPages-blackPages-(pageInProgressDisplayOptional.isPresent() ? 1 : 0);
		
		System.out.println(PHR.r("Total pages: {}", totalPages));
		System.out.println(PHR.r("Black Pages: {}", blackPages));
		if(pageInProgressDisplayOptional.isPresent())
			System.out.println(PHR.r("Page in progress: {}", pageInProgressDisplayOptional.get()));
		else
			System.out.println("(no page in progress)");
		System.out.println(PHR.r("White Pages: {}", whitePages));
	}
	
	private static Coordinate2DInt readPageLayout()
	{
		String inputString = readInput(PHR.r("Page layout ({}) (leave blank for DIN A4 with 5mm boxes):", PAGE_LAYOUT_FORMAT));
		if(inputString.isBlank())
			return getDefaultLayout();
		
		var split = StringUtil.split(inputString, "x");
		if(split.size() != 2)
			throwWrongFormat(inputString);
		
		int daysPerRow = Integer.parseInt(split.get(0));
		int numberOfRows = Integer.parseInt(split.get(1));
		var pageLayout = new Coordinate2DInt(daysPerRow, numberOfRows);
		
		System.out.println(PHR.r("Using layout: {}", displayPageLayout(pageLayout)));
		return pageLayout;
	}
	
	private static Optional<String> getPageInProgressDisplay(int daysLived, Coordinate2DInt pageLayout)
	{
		int daysPerPage = pageLayout.getX()*pageLayout.getY();
		int pageInProgressDays = daysLived%daysPerPage;
		if(pageInProgressDays == 0)
			return Optional.empty();
		
		double pageInProgressPercentage = MathUtil.round(pageInProgressDays/(double) daysPerPage*100, 1);
		String total = PHR.r("{} black days of {} days ({}%)", pageInProgressDays, daysPerPage, pageInProgressPercentage);
		
		int blackRows = pageInProgressDays/pageLayout.getX();
		int inProgressRowBlackDays = pageInProgressDays%pageLayout.getX();
		boolean isRowInProgress = inProgressRowBlackDays > 0;
		int inProgressRowWhiteDays = pageLayout.getX()-inProgressRowBlackDays;
		String inProgressRowDisplay = isRowInProgress ?
			PHR.r("row in progress with {} black days and {} white days", inProgressRowBlackDays, inProgressRowWhiteDays)
			: "(no row in progress)";
		int whiteRows = pageLayout.getY()-blackRows-(isRowInProgress ? 1 : 0);
		String inRows = PHR.r("{} black rows, {}, {} white rows", blackRows, inProgressRowDisplay, whiteRows);
		return Optional.of(total+"; "+inRows);
	}
	
	private static Coordinate2DInt getDefaultLayout()
	{
		final int dinA4WidthCm = 21;
		final double dinA4HeightCm = 29.5;
		final int numberOfBoxesPerCm = 2;
		
		int daysPerRow = dinA4WidthCm*numberOfBoxesPerCm;
		int numberOfRows = (int) Math.round(dinA4HeightCm*numberOfBoxesPerCm);
		var pageLayout = new Coordinate2DInt(daysPerRow, numberOfRows);
		
		System.out.println(PHR.r("No input, using default layout: {} (DIN A4 with 5mm boxes)", displayPageLayout(pageLayout)));
		return pageLayout;
	}
	
	private static String displayPageLayout(Coordinate2DInt pageLayout)
	{
		return PHR.r("{} days per row, {} rows", pageLayout.getX(), pageLayout.getY());
	}
	
	private static void throwWrongFormat(String inputString)
	{
		String message = PHR.r("Page layout has to be specified like '{}' but input was '{}'", PAGE_LAYOUT_FORMAT, inputString);
		throw new IllegalArgumentException(message);
	}
	
	
	private static LocalDate readInputDate(String prompt)
	{
		String input = readInput(prompt);
		return LocalDate.parse(input);
	}
	
	private static int readInputInteger(String prompt)
	{
		String input = readInput(prompt);
		try
		{
			return Integer.parseInt(input);
		}
		catch(NumberFormatException e)
		{
			String message = PHR.r("Failed to parse input as integer: '{}'", input);
			throw new IllegalArgumentException(message, e);
		}
	}
	
	private static String readInput(String prompt)
	{
		System.out.println(prompt);
		return new Scanner(System.in).nextLine();
	}
	
}
