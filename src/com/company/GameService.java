package com.company;

import com.company.Classes.*;

import java.util.*;

public class GameService {

    /**
     * добавление карт в игру
     * @param g
     */
    public void addCardsInGame(Game g) {
        ArrayList<Card> allCards = new ArrayList<>();
        Stack<Card> cards = new Stack<>();
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                allCards.add(new Card(rank, suit));
                Collections.shuffle(allCards);
            }
        }
        Card trump = allCards.remove(35);
        g.setTrump(trump);
        cards.push(trump);
        for (Card card : allCards) {
            cards.push(card);
        }
        g.setCards(cards);
    }

    /**
     * добавление игроков в игру
     * @param g
     * @param playersCount
     */
    public void addPlayersInGame(Game g, int playersCount) {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= playersCount; i++) {
            players.add(new Player(Integer.toString(i)));
        }
        g.setPlayers(players);
    }

    /**
     * запуск игры
     * @param g
     */
    public void play(Game g) {
        distortionCards(g);     //раздаем карты

        System.out.println(g.getTrump());                   //вывод козыря
        System.out.println(g.getPlayers());                 //вывод игроков
        System.out.println(g.getPlayersCards());            //вывод их карт

        Player source = getPlayerWhoMovedFirst(g);          // кто ходит первым
        Player target = getNextPlayingPlayer(g, source);    // кто отбивает

        Round firstRound = new Round(source, target);        // создаем 1ый раунд
        g.addRound(firstRound);                              // сохраняем инфрмацию о 1ом раунде
        source = GameService.playingRound(g, firstRound, true);  // играем 1 раунд и возвращаем игрока,
                                                                            // который ходит следующим

        target = getNextPlayingPlayer(g, source);           // находим игрока, который будет отбиваться следующим
        while (isGameActive(g)) {                           // пока (игра активна) {
            Round round = new Round(source, target);        // создаем новый раунд
            g.addRound(round);                              // сохраняем инфрмацию о раунде
            source = GameService.playingRound(g, round, false);
                                                            // играем раунд и возвращаем игрока,
                                                            // который ходит следующим

            target = getNextPlayingPlayer(g, source);       // находим игрока, который будет отбиваться следующим
        }                                                   // }

    }

    /**
     * раздача карт
     * @param g
     */
    private void distortionCards(Game g) {
        Map<Player, List<Card>> playersCards = new HashMap<>();     //лист с картами всех игорков
        for (Player p : g.getPlayers()) {

            List<Card> pc = new ArrayList<>();              //карты p-го игрока
            for (int i = 0; i < 6; i++) {                   //в цикле ему выдаются его карты
                pc.add(g.getCards().pop());
            }
            Collections.sort(pc);                           //его карты сортируются
            playersCards.put(p, pc);                        //его карты сохранятся в лист с
            // картами всех игроков
        }
        g.setPlayersCards(playersCards);                    //сохранятся лист с картами игроков
    }

    /**
     * возвращает следующего по очереди игрока, который ещё в игре
     * @param g
     * @param playerSource
     * @return
     */
    private static Player getNextPlayingPlayer(Game g, Player playerSource) {
        int sourcePlayerNumber = Integer.valueOf(playerSource.getNumber());
        List<Player> players = g.getPlayers();                              // игроки
        Player nextPlayingPlayer = null;                                    // кто будет биться следующим
        if (sourcePlayerNumber == players.size() - 1) {                 // если аттакующий под последним номером
            nextPlayingPlayer = players.get(0);                         // то защищающимся должен быть нулевой
        } else if (nextPlayingPlayer.equals(playerSource)) {            // если следующим явлется он сам,
            // то значит он остался 1 и игра окночена

        } else {
            nextPlayingPlayer = players.get(sourcePlayerNumber + 1);   // иначе защищающимся должен быть следующий
        }

        if (isPlayerActive(g, nextPlayingPlayer)) {   // если записанный следующий ущё в игре
            return nextPlayingPlayer;
        } else {
            return getNextPlayingPlayer(g, nextPlayingPlayer);  //то ищем следующего после него
        }
    }

    /**
     * разыгровка раунда
     * @param g
     * @param round
     * @param isFirstRound
     * @return
     */
    public static Player playingRound(Game g, Round round, boolean isFirstRound) {
        Player source = round.getSource();
        Player target = round.getTarget();
        List<Fight> fights = new ArrayList<>();
        int maxCountFights;
        if (isFirstRound) {
            maxCountFights = 5;
        } else {
            maxCountFights = 6;
        }
        int i = 0;

        while (fights.size() <= maxCountFights || fights.get(i).isCovered()) {
            Card down = GameService.moveAttacked(g, source);
            Card up = GameService.moveTargeted(g, target, down);
            Fight fight = new Fight(down, up);
            fights.add(fight);
            i++;
        }
        if (fights.get(i).isCovered()) {
            round.setPickedUp(true);
        } else {
            round.setPickedUp(false);
        }
        g.addRound(round);          //сохраняем раунд
        if (round.isPickedUp()) {
            return target;
        } else {
            //передать таргету все карты, которые были в этом бою
            return GameService.getNextPlayingPlayer(g, target);
        }
    }

    /**
     * ход аттакующего (возвращает карту которую надо будет побить)
     * @param g
     * @param player
     * @return
     */
    public static Card moveAttacked(Game g, Player player) {
        List<Card> cards = g.getPlayersCards().get(player); //ходит своей самой маленькой картой
        return cards.get(0);
    }

    /**
     * ход защищающегося(возвращает карту, которой будем бить карту down)
     * @param g
     * @param player
     * @param down
     * @return
     */
    public static Card moveTargeted(Game g, Player player, Card down) {
        //ищем самую маленькую карту, которой можем побить карту up
        //в разработке, на данный момент возвращает карту, которую надо побить
        return down;
    }


    /**
     * поиск игрока, ходящего самым первым
     * @param g
     * @return
     */
    private Player getPlayerWhoMovedFirst(Game g) { // кто ходит первым
        int minRang = 14;
        int maxRang = 0;
        Card currentTrump = g.getTrump(); // текущий козырь
        Player playerWhoMovedFirst = null;

        if (currentTrump.getRank().getRank() > 10) {
            playerWhoMovedFirst = searchMinTrumpInGame(g, currentTrump);
        } else {
            playerWhoMovedFirst = searchMaxTrumpInGame(g, currentTrump);
        }
        return  playerWhoMovedFirst;
    }

    /**
     * поиск максимального козыря на руках у игроков
     * @param g
     * @param currentTrump
     * @return
     */
    private Player searchMaxTrumpInGame(Game g, Card currentTrump) {
        Player playerWithMaxTrump = null;  //номер игрока с самым большим козырем
        Card minTrumpInGame = null;   //самый старший козырь из карт в игре
        for (int i = 0; i < g.getPlayers().size(); i++) {
            Player iPlayer = g.getPlayers().get(i);  // i-ый игрок
            List<Card> cardsIPlayer = g.getPlayersCards().get(iPlayer);  // карты i-го игроко
            Card maxTrumpInPlayer = null;   // наибольший козырь у игрока
            for (int j = cardsIPlayer.size() - 1; j > -1 ; j--) {
                if (cardsIPlayer.get(i).getSuit().equals(currentTrump)) {
                    if (maxTrumpInPlayer == null) {
                        maxTrumpInPlayer = cardsIPlayer.get(i);
                        break;
                    }
                }
            }
            for (Card card: cardsIPlayer) {
                if (card.getSuit().equals(currentTrump)) {
                    if (maxTrumpInPlayer == null) {
                        maxTrumpInPlayer = card;
                        break;
                    }
                }
            }
            if (minTrumpInGame == null && maxTrumpInPlayer != null) {
                minTrumpInGame = maxTrumpInPlayer;
                playerWithMaxTrump = iPlayer;
            } else {
                if (minTrumpInGame.compareTo(maxTrumpInPlayer) < 0) {       //если меньше 0, то заменяем
                    minTrumpInGame = maxTrumpInPlayer;
                    playerWithMaxTrump = iPlayer;
                }
            }
        }
        return playerWithMaxTrump;
    }

    /**
     * поиск минимамльного козыря на руках у игроков
     * @param g
     * @param currentTrump
     * @return
     */
    private Player searchMinTrumpInGame(Game g, Card currentTrump) { // search - поиск
        Player playerWithMinTrump = null;   // игрок с самым маленьким козырем
        Card minTrumpInGame = null;   // самый младший козырь из карт в игре
        for (int i = 0; i < g.getPlayers().size(); i++) {
            Player iPlayer = g.getPlayers().get(i);   // i-ый игрок
            List<Card> cardsIPlayer = g.getPlayersCards().get(iPlayer);  // карты i-го игрока
            Card minTrumpInPlayer = null;  // наименьший козырь у игрока
            for (Card card: cardsIPlayer) {
                if (card.getSuit().equals(currentTrump)) {                     // ??? если масть карты игрока совпадает с мастью козыря ???
                    if (minTrumpInPlayer == null) {                            // ??? зачем сравниваем с 0 ???
                        minTrumpInPlayer = card;
                        break;
                    }
                }
            }

            // ???

            if (minTrumpInGame == null && minTrumpInPlayer != null) {
                minTrumpInGame = minTrumpInPlayer;
                playerWithMinTrump = iPlayer;
            } else {
                if (minTrumpInGame.compareTo(minTrumpInPlayer) > 0) {       //если больше 0, то заменяем
                    minTrumpInGame = minTrumpInPlayer;
                    playerWithMinTrump = iPlayer;
                }
            }

            // ???

        }
        return playerWithMinTrump;
    }

    /**
     * проверка, активна ли ещё игра
     * @param g
     * @return
     */
    private boolean isGameActive(Game g) { // игра еще продолжается?
        int count = 0; // кол-во игроков
        for (Player p : g.getPlayers()) {
            if (!isPlayerActive(g, p)) {
                count++;
            }
        }
        if (!isDeckEmpty(g) || count > 1) {
            return true;
        }
        return false;
    }

    /**
     * проверка, активен ли ещё игрок (или уже вышел из игры)
     * @param g
     * @param p
     * @return
     */
    private static boolean isPlayerActive(Game g, Player p) { // игрок еще в игре?
        if (!g.getPlayersCards().get(p).isEmpty() || !isDeckEmpty(g)) {
            return true;
        }
        return false;
    }

    /**
     * проверка колоды на пустоту
     * @param g
     * @return
     */
    private static boolean isDeckEmpty(Game g) {
        if (g.getCards().isEmpty()) {
            return true;
        }
        return false;
    }
}