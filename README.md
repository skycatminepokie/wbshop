# Worldborder Shop
Fight the world border for every block! Donate items to expand the world border. Work together to unlock new biomes, find ways around scare resources, and compete for the top of the leaderboards.

## Commands
| Command | Effect | Permission level |
| ---- | ---- | ---- |
| `/wbshop` | Open the main gui. See how many points you and others have, the size of the worldborder, and the total number of points in the economy. | None |
| `/bal` | Get the number of points you have. | None |
| `/withdraw` | Withdraw your balance into a point voucher. Donate the point voucher to redeem your balance. | None |
| `/withdraw all` | Withdraw all of your balance. | None |
| `/wbshop econ` | Manage the economy | `wbshop.econ` or OP level 4 |
| `/wbshop econ borderfunction` | Set the function used to calculate the border size. Functions are parsed using [exp4j](https://github.com/fasseg/exp4j) and the variable "points." Make sure to use quotes around the function (ex: `/wbshop econ borderfunction "0.1points"` for 10 points per block wide) | `wbshop.econ` or OP level 4 |
| `/wbshop econ get` | Get the number of points that (a) player(s) has. | `wbshop.econ` or OP level 4 |
| `/wbshop econ add` | Add points to player(s). | `wbshop.econ` or OP level 4 |
| `/wbshop econ remove` | Remove points from player(s). | `wbshop.econ` or OP level 4 |
| `/wbshop econ total` | Get the total number of points in the economy. | `wbshop.econ` or OP level 4 |

### Probably important legal info
wbshop is licensed under the MIT license.

exp4j is embedded, which is licensed under Apache License 2.0. No modifications were made.
