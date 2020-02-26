
Sheriff of Nottingham ---Nitu Nicolae Iulian

	Sheriff of Nottingham is a mini-project that tries to implement a board game
logic, based on object oriented programming.
	Base logic is simple: players are splitted in 2 subcategories: merchants and
the sheriff, and each of them has a different behaviour deppending on what
strategy applies. Goods also are splitted in 2 opposite types: legal ones and
illegal ones.
	To implement this, I needed the following main classes: Player and Goods.
Player is the frame for the 3 strategies (Basic --- deriv. from Player, Greedy
and Bribe --- both deriv. from Basic) and his instances are created with a
PlayerFactory. The same pattern is applied for Goods (LegalGoods and IllegalGoods).

	The purpose for a player is to increase his initial budget by bringing goods
to the shop as many as he can as a merchant or by taking bribe or penalty from
other players as a sheriff. Because of that, each player has specific fields and
methods like:
 - money, bribe, profit, bag, shop, etc.
 - putLegal/IllegalGoods(), makeBag(), letMerchantGo(), checkItemBag(), etc.

Merchant:
	- initially has 10 cards in hand, each of them representing a good;
	- splits the goods in 2 categories: legals and illegals (both are hashMpaps:
	key = good id, value = quantity of that good);
	- his bag and shop has the same form --- hashMap<Integer, Integer>();
	- starts choosing items and put them in bag being aware not to put more than
	8, Basic and Greedy starts with legals and add illegals after and only if
	some conditions are satisfied, Bribe does the opposite.

Sheriff:
	- doesn't have cards, it is the one who inspects other players;
	- depending on what strategy applies, he can choose if and what players will
	inspect, Basic inspects all, Greedy's purpose is to take as much bribe as he
	can, Bribe is something between them;
	- he inspects only if he has money enough to pay the penalty if the merchant
	was honest.

Game Logic:
	- first of all I generate all players who take part of the game and set their
	strategies;
	- after each semiround, I switch the sheriff role;
	- during a subround, I first let merchants make their bag (merchantBehaviour)
	and then put sherif to check them (sheriffBehaviour);
	- I calculate illegal bonus and king-queen bonus in this order and total profit
	(which includes remaining money);
	- I sort and print the ranking.

Some special notes:
	- declaredGood = -1 means that Basic case when the player has only illegals
	in hand and not enough money to bring one of them to inspection and his bag
	will be empty;
	- for Basic (and implicitly Greedy), when the player puts the legals from
	legalGoods to bag, I don't remove them from legalGoods(hashMap), because there
	is no need: the map is iterated only once;
	- I make the same thing for Basic with illegals, but not for Greedy too,
	because Greedy can iterate twice in illegalGoods (if there's even round);
	- for Bribe (when he applies his strategy, not Basic), I always remove items
	from hashMaps once he put them in bag, because hashMap are iterated more times;
	- for Sheriff strategy, I have 2 methods that are similar: checkOneItemBag()
	and checkMoreItemsBag() because when the bag has only one item, I know exactly
	what is the penalty  (but I don't know which side has to pay it) and when
	there are more than 1 item, the penalty comes only from undeclared goods;
	- cards (goods) than do not bypass the inspection go to the bottom of the
	deck.