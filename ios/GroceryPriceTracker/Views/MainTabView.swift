import SwiftUI

struct MainTabView: View {
    @State private var selectedTab: Int = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            RecordListView()
                .tabItem {
                    Label("Home", systemImage: "house.fill")
                }
                .tag(0)

            PriceChartView()
                .tabItem {
                    Label("Chart", systemImage: "chart.line.uptrend.xyaxis")
                }
                .tag(1)

            ProductMapView()
                .tabItem {
                    Label("Map", systemImage: "map.fill")
                }
                .tag(2)
        }
    }
}

