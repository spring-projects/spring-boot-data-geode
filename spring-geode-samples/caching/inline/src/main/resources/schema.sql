CREATE TABLE IF NOT EXISTS calculations (
	operand INTEGER NOT NULL,
  	operator VARCHAR(256) NOT NULL,
	result INTEGER NOT NULL,
  	PRIMARY KEY (operand, operator)
);
