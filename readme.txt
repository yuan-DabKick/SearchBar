This is a customized search bar with two features
1.Center the query hint
2.Added animation for dismissing Query hint

1. To integrate
  1.In layout
    copy the custom_searchview.xml file to your layout section
  
  2.Include the custom searchview in your acitvity layout
    <include layout="@layout/custom_searchview"
        android:id="@+id/searchBar"></include>

  3.In acitvity java file
    //All classes are in MainActivity.java
    1. Copy and paste the class "CustomSearchViewHandler" in 
       /app/src/main/java/com/dabkick/developer3/searchbar/MainActivity.java
    2. Apply the handler to the searchbar
       => searchBarHandler = new CustomSearchViewHandler(this, searchBar);
    3. Setup listener for the handler
       => see setupSearchbar() for example of adding listener
       => Searchbar has a listener called onQueryTextEmpty(),you should
          reset the search result in this listener.
    4. Setup listView 
       => you can use setupListView() as an example to setup your
          adapter if needed.

2. Note
  1. We are waiting for X-button asset from Michael.
     This icon will be replaced later.
  2. The class "CustomAdapterTyping", we are not using it here. But 
     we keep it in project for future use. 
