package com.company;

import com.company.Classes.Game;

public class Main {

    public static void main(String[] args) {
        Game g = new Game();
        GameService s = new GameService();
        s.addCardsInGame(g);                    //добавление колоды
        s.addPlayersInGame(g, 3);    //добавление игроков
        s.play(g);                              //начало игры
    }
}
