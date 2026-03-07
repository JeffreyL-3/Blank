# Pathing JSON Generator

This repo includes a tool that converts a raw FTC autonomous Java file into a pathing JSON file.

## Tool

- `pathing_tool.py`

## Usage

```bash
python3 pathing_tool.py --input <auto_file.java> --output <pathing.json> --alliance blue --location far
```

Optional flags:

- `--alliance red`
- `--location close`
- `--force-close-score1`

## Included files

- `examples/ANewWorkingAuto.java` - example input source.
- `blue_far_pathing.json` - generated from the example using Blue/Far.
- `samples/provided_working_code.java` - sample source matching the provided working-code structure.
- `samples/provided_blue_far.json` - generated output from the provided sample source.

## Reproduce generation

```bash
python3 pathing_tool.py --input examples/ANewWorkingAuto.java --output blue_far_pathing.json --alliance blue --location far
python3 pathing_tool.py --input samples/provided_working_code.java --output samples/provided_blue_far.json --alliance blue --location far
```
