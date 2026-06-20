We compute metrics on each project using `qlty` tool, available at [https://github.com/qltysh/qlty](https://github.com/qltysh/qlty)

## Configure

```
qlty init
```

Two questions: 
1) no
2) yes


## Code Pretty

```
qlty fmt --all
```

## Remove possible directories from analysis

Edit the `.qlty/qlty.toml` file and include in the `exclude_patterns` section the directories you want to ignore.

```
subl .qlty/qlty.toml
```

## Lint Issues

```
qlty check --no-fix --all 2>&1 | ansi2txt > qlty-issues.txt
```

## Code Smells

```
qlty smells --all 2>&1 | ansi2txt > qlty-smells.txt
```

## Code Metrics

```
qlty metrics --sort name --dirs --all 2>&1 | ansi2txt > qlty-metrics.csv
```

## Run all commands after init

qlty fmt --all;qlty check --no-fix --all 2>&1 | ansi2txt > qlty-issues.txt;qlty smells --all 2>&1 | ansi2txt > qlty-smells.txt;qlty metrics --sort name --dirs --all 2>&1 | ansi2txt > qlty-metrics.csv