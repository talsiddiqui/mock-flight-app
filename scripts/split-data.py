import pandas as pd

# Load data
df = pd.read_csv("data/flights.csv", dtype={"FL_DATE": str})

# Ensure FL_DATE is in datetime format
df["FL_DATE"] = pd.to_datetime(df["FL_DATE"], format="%Y-%m-%d")

# Create year-month column
df["year_month"] = df["FL_DATE"].dt.strftime("%Y%m")

# Split and save
for ym, group in df.groupby("year_month"):
    group.drop(columns=["year_month"]).to_csv(f"data/{ym}-flights.csv", index=False)