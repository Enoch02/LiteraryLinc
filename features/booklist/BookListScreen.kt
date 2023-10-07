@OptIn(ExperimentalPagerApi::class)
@Composable
fun BookListScreen(modifier: Modifier, scope: CoroutineScope) {
    val pagerState = rememberPagerState()
    val mediaType = listOf("All", "Books", "Manga/LN", "Comics")

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            tabs = {
                mediaType.forEachIndexed { index, medium ->
                    Tab(
                        selected = index == pagerState.currentPage,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        content = {
                            Text(
                                text = medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    )
                }
            }
        )

        HorizontalPager(
            count = mediaType.size,
            state = pagerState,
            content = {
                BookListView(filter = it)
            }
        )
    }
}

@Composable
private fun BookListView(filter: Int, viewModel: MediaBookListScreenViewModel = viewModel()) {
    val media = viewModel.filterBooks(filter)

    if (media.isNotEmpty()) {
        LazyColumn(
            content = {
                items(
                    count = media.size,
                    itemContent = { index ->
                        val dummyItem = media[index]

                        Item(
                            title = dummyItem.title,
                            authors = dummyItem.authors,
                            type = dummyItem.type,
                            onClick = {})
                        /*if (index != dummyItems.size - 1) {
                            Divider()
                        }*/
                    }
                )
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Your list is empty!", fontWeight = FontWeight.Bold)
            Text(text = "Add Some Books to start tracking them.")
        }
    }
}

@Composable
private fun Item(title: String, authors: List<String>, onClick: () -> Unit, type: MediaType) {
    ListItem(
        overlineContent = {
            Text(text = type.name.replace("_", " or "))
        },
        headlineContent = {
            Text(
                text = title,
                fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = MaterialTheme.typography.titleMedium.fontWeight
            )
        },
        leadingContent = {
            Image(
                painter = painterResource(R.drawable.placeholder_image),
                contentDescription = "Dummy",
                modifier = Modifier.height(60.dp)
            )
        },
        supportingContent = {
            Text(
                text = if (authors.size > 5) {
                    authors.slice(0..5).joinToString(", ")
                } else {
                    authors.joinToString(", ")
                }
            )
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    BookListScreen(modifier = Modifier, rememberCoroutineScope())
}