create table users(
    id int auto_increment primary key,
	username varchar(50) not null primary key,
	password varchar(200) not null,
	enabled boolean not null
);

create table authorities (
    id int auto_increment primary key,
	username varchar(50) not null,
	authority varchar(50) not null,
	constraint fk_authorities_users foreign key(username) references users(username)
);

create table accounts (
    id int auto_increment primary key,
    accountOwner char(255),
    accountName char(255),
    accountType char(255),
    isPrimary bool,
    balance char(255),
    budgetStartDate char(255),
    budgetEndDate char(255)
);

create table budgets (
    id int auto_increment primary key,
    owner char(255),
    category char(255),
    archived bool,
    startDate char(255),
    endDate char(255),
    amount float
);

create table transactions (
    id int auto_increment primary key,
    owner char(255),
    income char(255),
    archived bool,
    date char(255),
    expense char(255),
    category char(255),
    account char(255)
);

create table budgetCategories (
    id int auto_increment primary key,
    title char(255),
    owner char(255)
);
