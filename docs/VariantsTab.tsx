import React, { useState } from 'react';
import {
  Search,
  Filter,
  Ruler,
  Image as ImageIcon,
  Plus,
  RefreshCw,
  FileText,
  Eye,
  Grid,
  MoreVertical,
  Edit2,
  ChevronLeft,
  ChevronRight,
  ChevronDown } from
'lucide-react';
const VARIANTS_DATA = [
{
  id: 1,
  name: 'Black',
  subtitle: 'P2-BLAC-BLA-22C7F3',
  sku: 'BAG-BLK-001',
  colorName: 'Black',
  colorHex: '#000000',
  status: 'Active',
  sizes: 6,
  images: 8,
  price: '₹1,299 - ₹2,499',
  image:
  'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=200&q=80'
},
{
  id: 2,
  name: 'Brown',
  subtitle: 'P2-BROW-BLA-22C7F3',
  sku: 'BAG-BRW-002',
  colorName: 'Brown',
  colorHex: '#8B4513',
  status: 'Active',
  sizes: 5,
  images: 6,
  price: '₹1,299 - ₹2,499',
  image:
  'https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=200&q=80'
},
{
  id: 3,
  name: 'Tan',
  subtitle: 'P2-TAN-BLA-22C7F3',
  sku: 'BAG-TAN-003',
  colorName: 'Tan',
  colorHex: '#D2B48C',
  status: 'Draft',
  sizes: 4,
  images: 5,
  price: '₹1,199 - ₹2,299',
  image:
  'https://images.unsplash.com/photo-1559563458-527698bf5295?auto=format&fit=crop&w=200&q=80'
},
{
  id: 4,
  name: 'Navy Blue',
  subtitle: 'P2-NAVY-BLA-22C7F3',
  sku: 'BAG-NAV-004',
  colorName: 'Navy Blue',
  colorHex: '#000080',
  status: 'Inactive',
  sizes: 3,
  images: 5,
  price: '₹1,199 - ₹2,299',
  image:
  'https://images.unsplash.com/photo-1591561954557-26941169b49e?auto=format&fit=crop&w=200&q=80'
}];

export function VariantsTab() {
  const [searchTerm, setSearchTerm] = useState('');
  const filteredVariants = VARIANTS_DATA.filter(
    (v) =>
    v.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    v.sku.toLowerCase().includes(searchTerm.toLowerCase())
  );
  return (
    <div className="bg-white rounded-2xl p-6 shadow-card border border-black/5 flex flex-col gap-8">
      {/* Header Section */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
        <div>
          <h2 className="text-3xl font-serif font-bold text-gray-900 mb-1">
            Product Variants
          </h2>
          <p className="text-gray-500">Manage all variants of this product</p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <div className="relative">
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search by SKU, color, variant name..."
              className="pl-9 pr-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brown/20 w-[280px]"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)} />
            
          </div>
          <HeaderButton icon={<Filter className="w-4 h-4" />}>
            Filters
          </HeaderButton>
          <HeaderButton icon={<Ruler className="w-4 h-4" />}>
            View Sizes
          </HeaderButton>
          <HeaderButton icon={<ImageIcon className="w-4 h-4" />}>
            View Images
          </HeaderButton>
          <button className="flex items-center gap-2 px-4 py-2.5 bg-brown text-white rounded-xl text-sm font-medium hover:bg-brown-dark transition-colors shadow-sm">
            <Plus className="w-4 h-4" />
            New Variant
          </button>
          <button className="p-2.5 border border-gray-200 rounded-xl text-gray-600 hover:bg-gray-50 transition-colors">
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Stats Row */}
      <div className="flex flex-wrap gap-4">
        <StatCard
          icon={<FileText className="w-5 h-5 text-brown" />}
          iconBg="bg-[#f5f2ec]"
          title="Total Variants"
          value="4"
          subtitle="All variants" />
        
        <StatCard
          icon={<div className="w-3 h-3 rounded-full bg-brand-green-text" />}
          iconBg="bg-brand-green-bg"
          title="Active Variants"
          value="3"
          subtitle="Currently active" />
        
        <StatCard
          icon={<Eye className="w-5 h-5 text-brand-blue-text" />}
          iconBg="bg-brand-blue-bg"
          title="Visible Variants"
          value="3"
          subtitle="Published variants" />
        
        <StatCard
          icon={<Grid className="w-5 h-5 text-orange-600" />}
          iconBg="bg-orange-50"
          title="Total Sizes"
          value="18"
          subtitle="Across all variants" />
        
        <StatCard
          icon={<ImageIcon className="w-5 h-5 text-purple-600" />}
          iconBg="bg-purple-50"
          title="Total Images"
          value="24"
          subtitle="Across all variants" />
        
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-gray-100">
              <th className="py-4 px-4 font-semibold text-sm text-gray-900">
                Variant
              </th>
              <th className="py-4 px-4 font-semibold text-sm text-gray-900">
                SKU
              </th>
              <th className="py-4 px-4 font-semibold text-sm text-gray-900">
                Color
              </th>
              <th className="py-4 px-4 font-semibold text-sm text-gray-900">
                Status
              </th>
              <th className="py-4 px-4 font-semibold text-sm text-gray-900 text-center">
                Sizes
              </th>
              <th className="py-4 px-4 font-semibold text-sm text-gray-900 text-center">
                Images
              </th>
              <th className="py-4 px-4 font-semibold text-sm text-gray-900">
                Price Ranges
              </th>
              <th className="py-4 px-4 font-semibold text-sm text-gray-900 text-center">
                Actions
              </th>
            </tr>
          </thead>
          <tbody>
            {filteredVariants.map((variant, idx) =>
            <tr
              key={variant.id}
              className="border-b border-gray-50 hover:bg-gray-50/50 transition-colors group">
              
                <td className="py-4 px-4">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 rounded-xl bg-[#f5f2ec] p-1 flex-shrink-0">
                      <img
                      src={variant.image}
                      alt={variant.name}
                      className="w-full h-full object-contain mix-blend-multiply" />
                    
                    </div>
                    <div>
                      <div className="font-semibold text-gray-900">
                        {variant.name}
                      </div>
                      <div className="text-xs text-gray-400 mt-0.5">
                        {variant.subtitle}
                      </div>
                    </div>
                  </div>
                </td>
                <td className="py-4 px-4 text-sm text-gray-600">
                  {variant.sku}
                </td>
                <td className="py-4 px-4">
                  <div className="flex items-center gap-2">
                    <div
                    className="w-3 h-3 rounded-full shadow-sm"
                    style={{
                      backgroundColor: variant.colorHex
                    }} />
                  
                    <span className="text-sm text-gray-600">
                      {variant.colorName}
                    </span>
                  </div>
                </td>
                <td className="py-4 px-4">
                  <StatusBadge status={variant.status} />
                </td>
                <td className="py-4 px-4 text-center">
                  <div className="font-semibold text-gray-900">
                    {variant.sizes}
                  </div>
                  <div className="text-xs text-gray-400 mt-0.5">View sizes</div>
                </td>
                <td className="py-4 px-4 text-center">
                  <div className="font-semibold text-gray-900">
                    {variant.images}
                  </div>
                  <div className="text-xs text-gray-400 mt-0.5">
                    View images
                  </div>
                </td>
                <td className="py-4 px-4 text-sm text-gray-600">
                  {variant.price}
                </td>
                <td className="py-4 px-4">
                  <div className="flex items-center justify-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button className="p-2 text-gray-400 hover:text-gray-900 hover:bg-white rounded-lg shadow-sm border border-transparent hover:border-gray-200 transition-all">
                      <Eye className="w-4 h-4" />
                    </button>
                    <button className="p-2 text-gray-400 hover:text-gray-900 hover:bg-white rounded-lg shadow-sm border border-transparent hover:border-gray-200 transition-all">
                      <Edit2 className="w-4 h-4" />
                    </button>
                    <button className="p-2 text-gray-400 hover:text-gray-900 hover:bg-white rounded-lg shadow-sm border border-transparent hover:border-gray-200 transition-all">
                      <MoreVertical className="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            )}
            {filteredVariants.length === 0 &&
            <tr>
                <td colSpan={8} className="py-8 text-center text-gray-500">
                  No variants found matching your search.
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>

      {/* Footer / Pagination */}
      <div className="flex items-center justify-between pt-4 border-t border-gray-100">
        <div className="text-sm text-gray-500">
          Showing 1 to {filteredVariants.length} of 4 variants
        </div>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2 px-3 py-1.5 border border-gray-200 rounded-lg text-sm text-gray-600 cursor-pointer hover:bg-gray-50">
            10 / page
            <ChevronDown className="w-4 h-4" />
          </div>
          <div className="flex items-center gap-1">
            <button
              className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-50 disabled:opacity-50"
              disabled>
              
              <ChevronLeft className="w-5 h-5" />
            </button>
            <button className="w-8 h-8 rounded-lg bg-brown text-white text-sm font-medium flex items-center justify-center">
              1
            </button>
            <button className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-50">
              <ChevronRight className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </div>);

}
// --- Subcomponents ---
function HeaderButton({
  children,
  icon



}: {children: React.ReactNode;icon: React.ReactNode;}) {
  return (
    <button className="flex items-center gap-2 px-4 py-2.5 border border-gray-200 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors bg-white">
      {icon}
      {children}
    </button>);

}
function StatCard({
  icon,
  iconBg,
  title,
  value,
  subtitle






}: {icon: React.ReactNode;iconBg: string;title: string;value: string;subtitle: string;}) {
  return (
    <div className="flex-1 min-w-[200px] bg-gray-50/50 rounded-2xl p-4 border border-gray-100 flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <div
          className={`w-10 h-10 rounded-xl flex items-center justify-center ${iconBg}`}>
          
          {icon}
        </div>
        <div className="text-xs font-medium text-gray-500">{title}</div>
      </div>
      <div>
        <div className="text-2xl font-bold text-gray-900">{value}</div>
        <div className="text-xs text-gray-400 mt-1">{subtitle}</div>
      </div>
    </div>);

}
function StatusBadge({ status }: {status: string;}) {
  if (status === 'Active') {
    return (
      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-brand-green-bg text-brand-green-text text-xs font-semibold">
        <div className="w-1.5 h-1.5 rounded-full bg-current" />
        Active
      </span>);

  }
  if (status === 'Draft') {
    return (
      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-brand-blue-bg text-brand-blue-text text-xs font-semibold">
        <div className="w-1.5 h-1.5 rounded-full bg-current" />
        Draft
      </span>);

  }
  return (
    <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-gray-100 text-gray-500 text-xs font-semibold">
      <div className="w-1.5 h-1.5 rounded-full bg-current" />
      Inactive
    </span>);

}