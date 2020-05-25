README.txt -- part of RiskOdds

RiskOdds is a simple program to calculate odds from the classic boardgame Risk.

See LICENSE for licensing information.

RiskOdds calculates all the nice odds in Risk you always wanted to know (or at
least I did).

**Results:**
```
1 attacking die  vs. 1 defending die :   (-0.5833, -0.4167) 
     (0,1): 41.67% 5/12     
     (1,0): 58.33% 7/12     
2 attacking dice vs. 1 defending die :   (-0.4213, -0.5787)
     (0,1): 57.87% 125/216
     (1,0): 42.13% 91/216
3 attacking dice vs. 1 defending die :   (-0.3403, -0.6597)
     (0,1): 65.97% 95/144
     (1,0): 34.03% 49/144
1 attacking die  vs. 2 defending dice:   (-0.7454, -0.2546)
     (0,1): 25.46% 55/216
     (1,0): 74.54% 161/216
2 attacking dice vs. 2 defending dice:   (-1.2207, -0.7793)
     (0,2): 22.76% 295/1296
     (1,1): 32.41% 35/108
     (2,0): 44.83% 581/1296
3 attacking dice vs. 2 defending dice:   (-0.9209, -1.0791)
     (0,2): 37.17% 1445/3888
     (1,1): 33.58% 2611/7776
     (2,0): 29.26% 2275/7776
```

**Usage:**
- By default this will print the dice odds.
- Invoked with java RiskOdds attackers defenders it will print the odds of
  the attackers winning with the specified army counts.
